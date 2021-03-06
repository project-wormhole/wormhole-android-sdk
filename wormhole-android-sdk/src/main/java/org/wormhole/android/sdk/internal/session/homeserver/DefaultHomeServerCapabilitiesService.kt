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

package org.wormhole.android.sdk.internal.session.homeserver

import com.zhuinden.monarchy.Monarchy
import io.realm.Realm
import org.wormhole.android.sdk.api.session.homeserver.HomeServerCapabilities
import org.wormhole.android.sdk.api.session.homeserver.HomeServerCapabilitiesService
import org.wormhole.android.sdk.internal.database.mapper.HomeServerCapabilitiesMapper
import org.wormhole.android.sdk.internal.database.model.HomeServerCapabilitiesEntity
import org.wormhole.android.sdk.internal.database.query.get
import org.wormhole.android.sdk.internal.di.SessionDatabase
import javax.inject.Inject

internal class DefaultHomeServerCapabilitiesService @Inject constructor(
        @SessionDatabase private val monarchy: Monarchy,
        private val getHomeServerCapabilitiesTask: GetHomeServerCapabilitiesTask
) : HomeServerCapabilitiesService {

    override suspend fun refreshHomeServerCapabilities() {
        getHomeServerCapabilitiesTask.execute(GetHomeServerCapabilitiesTask.Params(forceRefresh = true))
    }

    override fun getHomeServerCapabilities(): HomeServerCapabilities {
        return Realm.getInstance(monarchy.realmConfiguration).use { realm ->
            HomeServerCapabilitiesEntity.get(realm)?.let {
                HomeServerCapabilitiesMapper.map(it)
            }
        }
                ?: HomeServerCapabilities()
    }
}
