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

import org.wormhole.android.sdk.internal.network.GlobalErrorReceiver
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface ResolveSpaceInfoTask : Task<ResolveSpaceInfoTask.Params, SpacesResponse> {
    data class Params(
            val spaceId: String,
            val maxRoomPerSpace: Int?,
            val limit: Int,
            val batchToken: String?,
            val suggestedOnly: Boolean?,
            val autoJoinOnly: Boolean?
    ) {
        companion object {
            fun withId(spaceId: String, suggestedOnly: Boolean?, autoJoinOnly: Boolean?) =
                    Params(
                            spaceId = spaceId,
                            maxRoomPerSpace = 10,
                            limit = 20,
                            batchToken = null,
                            suggestedOnly = suggestedOnly,
                            autoJoinOnly = autoJoinOnly
                    )
        }
    }
}

internal class DefaultResolveSpaceInfoTask @Inject constructor(
        private val spaceApi: SpaceApi,
        private val globalErrorReceiver: GlobalErrorReceiver
) : ResolveSpaceInfoTask {
    override suspend fun execute(params: ResolveSpaceInfoTask.Params): SpacesResponse {
        val body = SpaceSummaryParams(
                maxRoomPerSpace = params.maxRoomPerSpace,
                limit = params.limit,
                batch = params.batchToken ?: "",
                autoJoinedOnly = params.autoJoinOnly,
                suggestedOnly = params.suggestedOnly
        )
        return executeRequest(globalErrorReceiver) {
            spaceApi.getSpaces(params.spaceId, body)
        }
    }
}
