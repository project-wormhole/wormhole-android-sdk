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

package org.wormhole.android.sdk.internal.session.space

import org.wormhole.android.sdk.api.query.QueryStringValue
import org.wormhole.android.sdk.api.session.events.model.EventType
import org.wormhole.android.sdk.api.session.events.model.toContent
import org.wormhole.android.sdk.api.session.events.model.toModel
import org.wormhole.android.sdk.api.session.room.Room
import org.wormhole.android.sdk.api.session.room.model.RoomSummary
import org.wormhole.android.sdk.api.session.space.Space
import org.wormhole.android.sdk.api.session.space.model.SpaceChildContent
import org.wormhole.android.sdk.internal.session.permalinks.ViaParameterFinder
import org.wormhole.android.sdk.internal.session.room.summary.RoomSummaryDataSource

internal class DefaultSpace(
        private val room: Room,
        private val spaceSummaryDataSource: RoomSummaryDataSource,
        private val viaParameterFinder: ViaParameterFinder
) : Space {

    override fun asRoom(): Room {
        return room
    }

    override val spaceId = room.roomId

    override suspend fun leave(reason: String?) {
        return room.leave(reason)
    }

    override fun spaceSummary(): RoomSummary? {
        return spaceSummaryDataSource.getSpaceSummary(room.roomId)
    }

    override suspend fun addChildren(roomId: String,
                                     viaServers: List<String>?,
                                     order: String?,
                                     autoJoin: Boolean,
                                     suggested: Boolean?) {
        // Find best via

        room.sendStateEvent(
                eventType = EventType.STATE_SPACE_CHILD,
                stateKey = roomId,
                body = SpaceChildContent(
                        via = viaServers ?: viaParameterFinder.computeViaParams(roomId, 3),
                        autoJoin = autoJoin,
                        order = order,
                        suggested = suggested
                ).toContent()
        )
    }

    override suspend fun removeChildren(roomId: String) {
//        val existing = room.getStateEvents(setOf(EventType.STATE_SPACE_CHILD), QueryStringValue.Equals(roomId))
//                .firstOrNull()
//                ?.content.toModel<SpaceChildContent>()
//                ?: // should we throw here?
//                return

        // edit state event and set via to null
        room.sendStateEvent(
                eventType = EventType.STATE_SPACE_CHILD,
                stateKey = roomId,
                body = SpaceChildContent(
                        order = null,
                        via = null,
                        autoJoin = null,
                        suggested = null
                ).toContent()
        )
    }

    override suspend fun setChildrenOrder(roomId: String, order: String?) {
        val existing = room.getStateEvents(setOf(EventType.STATE_SPACE_CHILD), QueryStringValue.Equals(roomId))
                .firstOrNull()
                ?.content.toModel<SpaceChildContent>()
                ?: throw IllegalArgumentException("$roomId is not a child of this space")

        // edit state event and set via to null
        room.sendStateEvent(
                eventType = EventType.STATE_SPACE_CHILD,
                stateKey = roomId,
                body = SpaceChildContent(
                        order = order,
                        via = existing.via,
                        autoJoin = existing.autoJoin,
                        suggested = existing.suggested
                ).toContent()
        )
    }

    override suspend fun setChildrenAutoJoin(roomId: String, autoJoin: Boolean) {
        val existing = room.getStateEvents(setOf(EventType.STATE_SPACE_CHILD), QueryStringValue.Equals(roomId))
                .firstOrNull()
                ?.content.toModel<SpaceChildContent>()
                ?: throw IllegalArgumentException("$roomId is not a child of this space")

        if (existing.autoJoin == autoJoin) {
            // nothing to do?
            return
        }

        // edit state event and set via to null
        room.sendStateEvent(
                eventType = EventType.STATE_SPACE_CHILD,
                stateKey = roomId,
                body = SpaceChildContent(
                        order = existing.order,
                        via = existing.via,
                        autoJoin = autoJoin,
                        suggested = existing.suggested
                ).toContent()
        )
    }

    override suspend fun setChildrenSuggested(roomId: String, suggested: Boolean) {
        val existing = room.getStateEvents(setOf(EventType.STATE_SPACE_CHILD), QueryStringValue.Equals(roomId))
                .firstOrNull()
                ?.content.toModel<SpaceChildContent>()
                ?: throw IllegalArgumentException("$roomId is not a child of this space")

        if (existing.suggested == suggested) {
            // nothing to do?
            return
        }
        // edit state event and set via to null
        room.sendStateEvent(
                eventType = EventType.STATE_SPACE_CHILD,
                stateKey = roomId,
                body = SpaceChildContent(
                        order = existing.order,
                        via = existing.via,
                        autoJoin = existing.autoJoin,
                        suggested = suggested
                ).toContent()
        )
    }
}
