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

package org.wormhole.android.sdk.internal.session.user.model

import org.wormhole.android.sdk.api.session.user.model.User
import org.wormhole.android.sdk.internal.network.GlobalErrorReceiver
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.session.user.SearchUserAPI
import org.wormhole.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface SearchUserTask : Task<SearchUserTask.Params, List<User>> {

    data class Params(
            val limit: Int,
            val search: String,
            val excludedUserIds: Set<String>
    )
}

internal class DefaultSearchUserTask @Inject constructor(
        private val searchUserAPI: SearchUserAPI,
        private val globalErrorReceiver: GlobalErrorReceiver
) : SearchUserTask {

    override suspend fun execute(params: SearchUserTask.Params): List<User> {
        val response = executeRequest(globalErrorReceiver) {
            searchUserAPI.searchUsers(SearchUsersParams(params.search, params.limit))
        }
        return response.users.map {
            User(it.userId, it.displayName, it.avatarUrl)
        }
    }
}
