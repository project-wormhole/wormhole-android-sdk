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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.wormhole.android.sdk.api.session.events.model.Event

@JsonClass(generateAdapter = true)
internal data class SpacesResponse(
        /** Its presence indicates that there are more results to return. */
        @Json(name = "next_batch") val nextBatch: String? = null,
        /** Rooms information like name/avatar/type ... */
        @Json(name = "rooms") val rooms: List<SpaceChildSummaryResponse>? = null,
        /** These are the edges of the graph. The objects in the array are complete (or stripped?) m.room.parent or m.space.child events. */
        @Json(name = "events") val events: List<Event>? = null
)
