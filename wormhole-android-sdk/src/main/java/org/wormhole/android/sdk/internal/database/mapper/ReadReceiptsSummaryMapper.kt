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

package org.wormhole.android.sdk.internal.database.mapper

import org.wormhole.android.sdk.api.session.room.model.ReadReceipt
import org.wormhole.android.sdk.internal.database.RealmSessionProvider
import org.wormhole.android.sdk.internal.database.model.ReadReceiptsSummaryEntity
import org.wormhole.android.sdk.internal.database.model.UserEntity
import org.wormhole.android.sdk.internal.database.query.where
import javax.inject.Inject

internal class ReadReceiptsSummaryMapper @Inject constructor(private val realmSessionProvider: RealmSessionProvider) {

    fun map(readReceiptsSummaryEntity: ReadReceiptsSummaryEntity?): List<ReadReceipt> {
        if (readReceiptsSummaryEntity == null) {
            return emptyList()
        }
        return realmSessionProvider.withRealm { realm ->
            val readReceipts = readReceiptsSummaryEntity.readReceipts
            readReceipts
                    .mapNotNull {
                        val user = UserEntity.where(realm, it.userId).findFirst()
                                ?: return@mapNotNull null
                        ReadReceipt(user.asDomain(), it.originServerTs.toLong())
                    }
        }
    }
}
