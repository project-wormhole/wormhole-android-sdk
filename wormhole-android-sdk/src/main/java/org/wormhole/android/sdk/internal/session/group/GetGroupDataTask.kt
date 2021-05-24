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

package org.wormhole.android.sdk.internal.session.group

import com.zhuinden.monarchy.Monarchy
import org.wormhole.android.sdk.api.session.room.model.Membership
import org.wormhole.android.sdk.internal.database.model.GroupEntity
import org.wormhole.android.sdk.internal.database.model.GroupSummaryEntity
import org.wormhole.android.sdk.internal.database.query.getOrCreate
import org.wormhole.android.sdk.internal.database.query.where
import org.wormhole.android.sdk.internal.di.SessionDatabase
import org.wormhole.android.sdk.internal.network.GlobalErrorReceiver
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.session.group.model.GroupRooms
import org.wormhole.android.sdk.internal.session.group.model.GroupSummaryResponse
import org.wormhole.android.sdk.internal.session.group.model.GroupUsers
import org.wormhole.android.sdk.internal.task.Task
import org.wormhole.android.sdk.internal.util.awaitTransaction
import timber.log.Timber
import javax.inject.Inject

internal interface GetGroupDataTask : Task<GetGroupDataTask.Params, Unit> {
    sealed class Params {
        object FetchAllActive : Params()
        data class FetchWithIds(val groupIds: List<String>) : Params()
    }
}

internal class DefaultGetGroupDataTask @Inject constructor(
        private val groupAPI: GroupAPI,
        @SessionDatabase private val monarchy: Monarchy,
        private val globalErrorReceiver: GlobalErrorReceiver
) : GetGroupDataTask {

    private data class GroupData(
            val groupId: String,
            val groupSummary: GroupSummaryResponse,
            val groupRooms: GroupRooms,
            val groupUsers: GroupUsers
    )

    override suspend fun execute(params: GetGroupDataTask.Params) {
        val groupIds = when (params) {
            is GetGroupDataTask.Params.FetchAllActive -> {
                getActiveGroupIds()
            }
            is GetGroupDataTask.Params.FetchWithIds   -> {
                params.groupIds
            }
        }
        Timber.v("Fetch data for group with ids: ${groupIds.joinToString(";")}")
        val data = groupIds.map { groupId ->
            val groupSummary = executeRequest(globalErrorReceiver) {
                groupAPI.getSummary(groupId)
            }
            val groupRooms = executeRequest(globalErrorReceiver) {
                groupAPI.getRooms(groupId)
            }
            val groupUsers = executeRequest(globalErrorReceiver) {
                groupAPI.getUsers(groupId)
            }
            GroupData(groupId, groupSummary, groupRooms, groupUsers)
        }
        insertInDb(data)
    }

    private fun getActiveGroupIds(): List<String> {
        return monarchy.fetchAllMappedSync(
                { realm ->
                    GroupEntity.where(realm, Membership.activeMemberships())
                },
                { it.groupId }
        )
    }

    private suspend fun insertInDb(groupDataList: List<GroupData>) {
        monarchy
                .awaitTransaction { realm ->
                    groupDataList.forEach { groupData ->

                        val groupSummaryEntity = GroupSummaryEntity.getOrCreate(realm, groupData.groupId)

                        groupSummaryEntity.avatarUrl = groupData.groupSummary.profile?.avatarUrl ?: ""
                        val name = groupData.groupSummary.profile?.name
                        groupSummaryEntity.displayName = if (name.isNullOrEmpty()) groupData.groupId else name
                        groupSummaryEntity.shortDescription = groupData.groupSummary.profile?.shortDescription ?: ""

                        groupSummaryEntity.roomIds.clear()
                        groupData.groupRooms.rooms.mapTo(groupSummaryEntity.roomIds) { it.roomId }

                        groupSummaryEntity.userIds.clear()
                        groupData.groupUsers.users.mapTo(groupSummaryEntity.userIds) { it.userId }
                    }
                }
    }
}
