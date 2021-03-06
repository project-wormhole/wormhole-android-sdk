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

package org.wormhole.android.sdk.api.session.room.send

/**
 * Describes a user draft:
 * REGULAR: draft of a classical message
 * QUOTE: draft of a message which quotes another message
 * EDIT: draft of an edition of a message
 * REPLY: draft of a reply of another message
 */
sealed class UserDraft(open val text: String) {
    data class REGULAR(override val text: String) : UserDraft(text)
    data class QUOTE(val linkedEventId: String, override val text: String) : UserDraft(text)
    data class EDIT(val linkedEventId: String, override val text: String) : UserDraft(text)
    data class REPLY(val linkedEventId: String, override val text: String) : UserDraft(text)

    fun isValid(): Boolean {
        return when (this) {
            is REGULAR -> text.isNotBlank()
            else       -> true
        }
    }
}
