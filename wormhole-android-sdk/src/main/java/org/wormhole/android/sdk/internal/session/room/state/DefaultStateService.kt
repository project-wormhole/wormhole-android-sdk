/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wormhole.android.sdk.internal.session.room.state

import android.net.Uri
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import org.wormhole.android.sdk.api.query.QueryStringValue
import org.wormhole.android.sdk.api.session.events.model.Event
import org.wormhole.android.sdk.api.session.events.model.EventType
import org.wormhole.android.sdk.api.session.events.model.toContent
import org.wormhole.android.sdk.api.session.room.model.GuestAccess
import org.wormhole.android.sdk.api.session.room.model.RoomCanonicalAliasContent
import org.wormhole.android.sdk.api.session.room.model.RoomHistoryVisibility
import org.wormhole.android.sdk.api.session.room.model.RoomJoinRules
import org.wormhole.android.sdk.api.session.room.state.StateService
import org.wormhole.android.sdk.api.util.JsonDict
import org.wormhole.android.sdk.api.util.MimeTypes
import org.wormhole.android.sdk.api.util.Optional
import org.wormhole.android.sdk.internal.session.content.FileUploader
import java.lang.UnsupportedOperationException

internal class DefaultStateService @AssistedInject constructor(@Assisted private val roomId: String,
                                                               private val stateEventDataSource: StateEventDataSource,
                                                               private val sendStateTask: SendStateTask,
                                                               private val fileUploader: FileUploader
) : StateService {

    @AssistedFactory
    interface Factory {
        fun create(roomId: String): DefaultStateService
    }

    override fun getStateEvent(eventType: String, stateKey: QueryStringValue): Event? {
        return stateEventDataSource.getStateEvent(roomId, eventType, stateKey)
    }

    override fun getStateEventLive(eventType: String, stateKey: QueryStringValue): LiveData<Optional<Event>> {
        return stateEventDataSource.getStateEventLive(roomId, eventType, stateKey)
    }

    override fun getStateEvents(eventTypes: Set<String>, stateKey: QueryStringValue): List<Event> {
        return stateEventDataSource.getStateEvents(roomId, eventTypes, stateKey)
    }

    override fun getStateEventsLive(eventTypes: Set<String>, stateKey: QueryStringValue): LiveData<List<Event>> {
        return stateEventDataSource.getStateEventsLive(roomId, eventTypes, stateKey)
    }

    override suspend fun sendStateEvent(
            eventType: String,
            stateKey: String?,
            body: JsonDict
    ) {
        val params = SendStateTask.Params(
                roomId = roomId,
                stateKey = stateKey,
                eventType = eventType,
                body = body.toSafeJson(eventType)
        )
        sendStateTask.executeRetry(params, 3)
    }

    private fun JsonDict.toSafeJson(eventType: String): JsonDict {
        // Safe treatment for PowerLevelContent
        return when (eventType) {
            EventType.STATE_ROOM_POWER_LEVELS -> toSafePowerLevelsContentDict()
            else                              -> this
        }
    }

    override suspend fun updateTopic(topic: String) {
        sendStateEvent(
                eventType = EventType.STATE_ROOM_TOPIC,
                body = mapOf("topic" to topic),
                stateKey = null
        )
    }

    override suspend fun updateName(name: String) {
        sendStateEvent(
                eventType = EventType.STATE_ROOM_NAME,
                body = mapOf("name" to name),
                stateKey = null
        )
    }

    override suspend fun updateCanonicalAlias(alias: String?, altAliases: List<String>) {
        sendStateEvent(
                eventType = EventType.STATE_ROOM_CANONICAL_ALIAS,
                body = RoomCanonicalAliasContent(
                        canonicalAlias = alias,
                        alternativeAliases = altAliases
                                // Ensure there is no duplicate
                                .distinct()
                                // Ensure the canonical alias is not also included in the alt alias
                                .minus(listOfNotNull(alias))
                                // Sort for the cleanup
                                .sorted()
                ).toContent(),
                stateKey = null
        )
    }

    override suspend fun updateHistoryReadability(readability: RoomHistoryVisibility) {
        sendStateEvent(
                eventType = EventType.STATE_ROOM_HISTORY_VISIBILITY,
                body = mapOf("history_visibility" to readability),
                stateKey = null
        )
    }

    override suspend fun updateJoinRule(joinRules: RoomJoinRules?, guestAccess: GuestAccess?) {
        if (joinRules != null) {
            if (joinRules == RoomJoinRules.RESTRICTED) throw UnsupportedOperationException("No yet supported")
            sendStateEvent(
                    eventType = EventType.STATE_ROOM_JOIN_RULES,
                    body = mapOf("join_rule" to joinRules),
                    stateKey = null
            )
        }
        if (guestAccess != null) {
            sendStateEvent(
                    eventType = EventType.STATE_ROOM_GUEST_ACCESS,
                    body = mapOf("guest_access" to guestAccess),
                    stateKey = null
            )
        }
    }

    override suspend fun updateAvatar(avatarUri: Uri, fileName: String) {
        val response = fileUploader.uploadFromUri(avatarUri, fileName, MimeTypes.Jpeg)
        sendStateEvent(
                eventType = EventType.STATE_ROOM_AVATAR,
                body = mapOf("url" to response.contentUri),
                stateKey = null
        )
    }

    override suspend fun deleteAvatar() {
        sendStateEvent(
                eventType = EventType.STATE_ROOM_AVATAR,
                body = emptyMap(),
                stateKey = null
        )
    }
}
