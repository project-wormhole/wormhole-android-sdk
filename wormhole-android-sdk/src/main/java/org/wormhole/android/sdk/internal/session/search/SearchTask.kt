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

package org.wormhole.android.sdk.internal.session.search

import org.wormhole.android.sdk.api.session.search.EventAndSender
import org.wormhole.android.sdk.api.session.search.SearchResult
import org.wormhole.android.sdk.api.util.MatrixItem
import org.wormhole.android.sdk.internal.network.GlobalErrorReceiver
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestBody
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestCategories
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestEventContext
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestFilter
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestOrder
import org.wormhole.android.sdk.internal.session.search.request.SearchRequestRoomEvents
import org.wormhole.android.sdk.internal.session.search.response.SearchResponse
import org.wormhole.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface SearchTask : Task<SearchTask.Params, SearchResult> {

    data class Params(
            val searchTerm: String,
            val roomId: String,
            val nextBatch: String? = null,
            val orderByRecent: Boolean,
            val limit: Int,
            val beforeLimit: Int,
            val afterLimit: Int,
            val includeProfile: Boolean
    )
}

internal class DefaultSearchTask @Inject constructor(
        private val searchAPI: SearchAPI,
        private val globalErrorReceiver: GlobalErrorReceiver
) : SearchTask {

    override suspend fun execute(params: SearchTask.Params): SearchResult {
        val searchRequestBody = SearchRequestBody(
                searchCategories = SearchRequestCategories(
                        roomEvents = SearchRequestRoomEvents(
                                searchTerm = params.searchTerm,
                                orderBy = if (params.orderByRecent) SearchRequestOrder.RECENT else SearchRequestOrder.RANK,
                                filter = SearchRequestFilter(
                                        limit = params.limit,
                                        rooms = listOf(params.roomId)
                                ),
                                eventContext = SearchRequestEventContext(
                                        beforeLimit = params.beforeLimit,
                                        afterLimit = params.afterLimit,
                                        includeProfile = params.includeProfile
                                )
                        )
                )
        )
        return executeRequest(globalErrorReceiver) {
            searchAPI.search(params.nextBatch, searchRequestBody)
        }.toDomain()
    }

    private fun SearchResponse.toDomain(): SearchResult {
        return SearchResult(
                nextBatch = searchCategories.roomEvents?.nextBatch,
                highlights = searchCategories.roomEvents?.highlights,
                results = searchCategories.roomEvents?.results?.map { searchResponseItem ->
                    EventAndSender(
                            searchResponseItem.event,
                            searchResponseItem.event.senderId?.let { senderId ->
                                searchResponseItem.context?.profileInfo?.get(senderId)
                                        ?.let {
                                            MatrixItem.UserItem(
                                                    senderId,
                                                    it["displayname"] as? String,
                                                    it["avatar_url"] as? String
                                            )
                                        }
                            }
                    )
                }?.reversed()
        )
    }
}
