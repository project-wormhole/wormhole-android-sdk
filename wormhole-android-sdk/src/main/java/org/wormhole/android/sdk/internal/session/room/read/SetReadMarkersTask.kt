/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wormhole.android.sdk.internal.session.room.read

import com.zhuinden.monarchy.Monarchy
import org.wormhole.android.sdk.api.session.events.model.LocalEcho
import org.wormhole.android.sdk.internal.database.model.RoomSummaryEntity
import org.wormhole.android.sdk.internal.database.model.TimelineEventEntity
import org.wormhole.android.sdk.internal.database.query.isEventRead
import org.wormhole.android.sdk.internal.database.query.isReadMarkerMoreRecent
import org.wormhole.android.sdk.internal.database.query.latestEvent
import org.wormhole.android.sdk.internal.database.query.where
import org.wormhole.android.sdk.internal.di.SessionDatabase
import org.wormhole.android.sdk.internal.di.UserId
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.session.room.RoomAPI
import org.wormhole.android.sdk.internal.session.sync.ReadReceiptHandler
import org.wormhole.android.sdk.internal.session.sync.RoomFullyReadHandler
import org.wormhole.android.sdk.internal.task.Task
import org.wormhole.android.sdk.internal.util.awaitTransaction
import io.realm.Realm
import org.wormhole.android.sdk.internal.network.GlobalErrorReceiver
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.set

internal interface SetReadMarkersTask : Task<SetReadMarkersTask.Params, Unit> {

    data class Params(
            val roomId: String,
            val fullyReadEventId: String? = null,
            val readReceiptEventId: String? = null,
            val forceReadReceipt: Boolean = false,
            val forceReadMarker: Boolean = false
    )
}

private const val READ_MARKER = "m.fully_read"
private const val READ_RECEIPT = "m.read"

internal class DefaultSetReadMarkersTask @Inject constructor(
        private val roomAPI: RoomAPI,
        @SessionDatabase private val monarchy: Monarchy,
        private val roomFullyReadHandler: RoomFullyReadHandler,
        private val readReceiptHandler: ReadReceiptHandler,
        @UserId private val userId: String,
        private val globalErrorReceiver: GlobalErrorReceiver
) : SetReadMarkersTask {

    override suspend fun execute(params: SetReadMarkersTask.Params) {
        val markers = mutableMapOf<String, String>()
        Timber.v("Execute set read marker with params: $params")
        val latestSyncedEventId = latestSyncedEventId(params.roomId)
        val fullyReadEventId = if (params.forceReadMarker) {
            latestSyncedEventId
        } else {
            params.fullyReadEventId
        }
        val readReceiptEventId = if (params.forceReadReceipt) {
            latestSyncedEventId
        } else {
            params.readReceiptEventId
        }
        if (fullyReadEventId != null && !isReadMarkerMoreRecent(monarchy.realmConfiguration, params.roomId, fullyReadEventId)) {
            if (LocalEcho.isLocalEchoId(fullyReadEventId)) {
                Timber.w("Can't set read marker for local event $fullyReadEventId")
            } else {
                markers[READ_MARKER] = fullyReadEventId
            }
        }
        if (readReceiptEventId != null
                && !isEventRead(monarchy.realmConfiguration, userId, params.roomId, readReceiptEventId)) {
            if (LocalEcho.isLocalEchoId(readReceiptEventId)) {
                Timber.w("Can't set read receipt for local event $readReceiptEventId")
            } else {
                markers[READ_RECEIPT] = readReceiptEventId
            }
        }

        val shouldUpdateRoomSummary = readReceiptEventId != null && readReceiptEventId == latestSyncedEventId
        if (markers.isNotEmpty() || shouldUpdateRoomSummary) {
            updateDatabase(params.roomId, markers, shouldUpdateRoomSummary)
        }
        if (markers.isNotEmpty()) {
            executeRequest(
                    globalErrorReceiver,
                    canRetry = true
            ) {
                if (markers[READ_MARKER] == null) {
                    if (readReceiptEventId != null) {
                        roomAPI.sendReceipt(params.roomId, READ_RECEIPT, readReceiptEventId)
                    }
                } else {
                    // "m.fully_read" value is mandatory to make this call
                    roomAPI.sendReadMarker(params.roomId, markers)
                }
            }
        }
    }

    private fun latestSyncedEventId(roomId: String): String? =
            Realm.getInstance(monarchy.realmConfiguration).use { realm ->
                TimelineEventEntity.latestEvent(realm, roomId = roomId, includesSending = false)?.eventId
            }

    private suspend fun updateDatabase(roomId: String, markers: Map<String, String>, shouldUpdateRoomSummary: Boolean) {
        monarchy.awaitTransaction { realm ->
            val readMarkerId = markers[READ_MARKER]
            val readReceiptId = markers[READ_RECEIPT]
            if (readMarkerId != null) {
                roomFullyReadHandler.handle(realm, roomId, FullyReadContent(readMarkerId))
            }
            if (readReceiptId != null) {
                val readReceiptContent = ReadReceiptHandler.createContent(userId, readReceiptId)
                readReceiptHandler.handle(realm, roomId, readReceiptContent, false, null)
            }
            if (shouldUpdateRoomSummary) {
                val roomSummary = RoomSummaryEntity.where(realm, roomId).findFirst()
                        ?: return@awaitTransaction
                roomSummary.notificationCount = 0
                roomSummary.highlightCount = 0
                roomSummary.hasUnreadMessages = false
            }
        }
    }
}
