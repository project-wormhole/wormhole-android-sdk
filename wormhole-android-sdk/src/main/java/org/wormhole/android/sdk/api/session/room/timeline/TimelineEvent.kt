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

package org.wormhole.android.sdk.api.session.room.timeline

import org.wormhole.android.sdk.BuildConfig
import org.wormhole.android.sdk.api.session.events.model.Event
import org.wormhole.android.sdk.api.session.events.model.EventType
import org.wormhole.android.sdk.api.session.events.model.RelationType
import org.wormhole.android.sdk.api.session.events.model.getRelationContent
import org.wormhole.android.sdk.api.session.events.model.isEdition
import org.wormhole.android.sdk.api.session.events.model.isReply
import org.wormhole.android.sdk.api.session.events.model.toModel
import org.wormhole.android.sdk.api.session.room.model.EventAnnotationsSummary
import org.wormhole.android.sdk.api.session.room.model.ReadReceipt
import org.wormhole.android.sdk.api.session.room.model.message.MessageContent
import org.wormhole.android.sdk.api.session.room.model.message.MessageStickerContent
import org.wormhole.android.sdk.api.session.room.model.relation.RelationDefaultContent
import org.wormhole.android.sdk.api.session.room.sender.SenderInfo
import org.wormhole.android.sdk.api.util.ContentUtils.extractUsefulTextFromReply

/**
 * This data class is a wrapper around an Event. It allows to get useful data in the context of a timeline.
 * This class is used by [TimelineService]
 * Users can also enrich it with metadata.
 */
data class TimelineEvent(
        val root: Event,
        /**
         * Uniquely identify an event, computed locally by the sdk
         */
        val localId: Long,
        val eventId: String,
        val displayIndex: Int,
        val senderInfo: SenderInfo,
        val annotations: EventAnnotationsSummary? = null,
        val readReceipts: List<ReadReceipt> = emptyList()
) {

    init {
        if (BuildConfig.DEBUG) {
            assert(eventId == root.eventId)
        }
    }

    val roomId = root.roomId ?: ""

    val metadata = HashMap<String, Any>()

    /**
     * The method to enrich this timeline event.
     * If you provides multiple data with the same key, only first one will be kept.
     * @param key the key to associate data with.
     * @param data the data to enrich with.
     */
    fun enrichWith(key: String?, data: Any?) {
        if (key == null || data == null) {
            return
        }
        if (!metadata.containsKey(key)) {
            metadata[key] = data
        }
    }

    /**
     * Get the metadata associated with a key.
     * @param key the key to get the metadata
     * @return the metadata
     */
    inline fun <reified T> getMetadata(key: String): T? {
        return metadata[key] as T?
    }

    fun isEncrypted(): Boolean {
        // warning: Do not use getClearType here
        return EventType.ENCRYPTED == root.type
    }
}

/**
 * Tells if the event has been edited
 */
fun TimelineEvent.hasBeenEdited() = annotations?.editSummary != null

/**
 * Get the latest known eventId for an edited event, or the eventId for an Event which has not been edited
 */
fun TimelineEvent.getLatestEventId(): String {
    return annotations
            ?.editSummary
            ?.sourceEvents
            ?.lastOrNull()
            ?: eventId
}

/**
 * Get the relation content if any
 */
fun TimelineEvent.getRelationContent(): RelationDefaultContent? {
    return root.getRelationContent()
}

/**
 * Get the eventId which was edited by this event if any
 */
fun TimelineEvent.getEditedEventId(): String? {
    return getRelationContent()?.takeIf { it.type == RelationType.REPLACE }?.eventId
}

/**
 * Get last MessageContent, after a possible edition
 */
fun TimelineEvent.getLastMessageContent(): MessageContent? {
    return if (root.getClearType() == EventType.STICKER) {
        root.getClearContent().toModel<MessageStickerContent>()
    } else {
        (annotations?.editSummary?.latestContent ?: root.getClearContent()).toModel()
    }
}

/**
 * Get last Message body, after a possible edition
 */
fun TimelineEvent.getLastMessageBody(): String? {
    val lastMessageContent = getLastMessageContent()

    if (lastMessageContent != null) {
        return lastMessageContent.newContent?.toModel<MessageContent>()?.body
                ?: lastMessageContent.body
    }

    return null
}

/**
 * Returns true if it's a reply
 */
fun TimelineEvent.isReply(): Boolean {
    return root.isReply()
}

fun TimelineEvent.isEdition(): Boolean {
    return root.isEdition()
}

fun TimelineEvent.getTextEditableContent(): String? {
    val lastContent = getLastMessageContent()
    return if (isReply()) {
        return extractUsefulTextFromReply(lastContent?.body ?: "")
    } else {
        lastContent?.body ?: ""
    }
}
