/*
 * Copyright (c) 2021 The Matrix.org Foundation C.I.C.
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

package org.wormhole.android.sdk.api.session.room

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import org.wormhole.android.sdk.api.session.room.model.RoomSummary

interface UpdatableLivePageResult {
    val livePagedList: LiveData<PagedList<RoomSummary>>

    fun updateQuery(builder: (RoomSummaryQueryParams) -> RoomSummaryQueryParams)

    val liveBoundaries: LiveData<ResultBoundaries>
}

data class ResultBoundaries(
        val frontLoaded: Boolean = false,
        val endLoaded: Boolean = false,
        val zeroItemLoaded: Boolean = false
)