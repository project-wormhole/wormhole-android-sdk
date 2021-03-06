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

package org.wormhole.android.sdk.internal.session.sync

import com.zhuinden.monarchy.Monarchy
import org.wormhole.android.sdk.internal.database.model.SyncEntity
import org.wormhole.android.sdk.internal.di.SessionDatabase
import io.realm.Realm
import javax.inject.Inject

internal class SyncTokenStore @Inject constructor(@SessionDatabase private val monarchy: Monarchy) {

    fun getLastToken(): String? {
        return Realm.getInstance(monarchy.realmConfiguration).use {
            it.where(SyncEntity::class.java).findFirst()?.nextBatch
        }
    }

    fun saveToken(realm: Realm, token: String?) {
        val sync = SyncEntity(token)
        realm.insertOrUpdate(sync)
    }
}
