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

@JsonClass(generateAdapter = true)
internal data class SpaceSummaryParams(
        /**  The maximum number of rooms/subspaces to return for a given space, if negative unbounded. default: -1 */
        @Json(name = "max_rooms_per_space") val maxRoomPerSpace: Int?,
        /** The maximum number of rooms/subspaces to return, server can override this, default: 100 */
        @Json(name = "limit") val limit: Int?,
        /** A token to use if this is a subsequent HTTP hit, default: "". */
        @Json(name = "batch") val batch: String = "",
        /** whether we should only return children with the "suggested" flag set. */
        @Json(name = "suggested_only") val suggestedOnly: Boolean?,
        /** whether we should only return children with the "suggested" flag set. */
        @Json(name = "auto_join_only") val autoJoinedOnly: Boolean?
)
