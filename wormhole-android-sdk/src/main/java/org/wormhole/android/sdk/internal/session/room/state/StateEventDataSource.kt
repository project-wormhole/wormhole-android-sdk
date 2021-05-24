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

package org.wormhole.android.sdk.internal.session.room.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.zhuinden.monarchy.Monarchy
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.kotlin.where
import org.wormhole.android.sdk.api.query.QueryStringValue
import org.wormhole.android.sdk.api.session.events.model.Event
import org.wormhole.android.sdk.api.util.Optional
import org.wormhole.android.sdk.api.util.toOptional
import org.wormhole.android.sdk.internal.database.RealmSessionProvider
import org.wormhole.android.sdk.internal.database.mapper.asDomain
import org.wormhole.android.sdk.internal.database.model.CurrentStateEventEntity
import org.wormhole.android.sdk.internal.database.model.CurrentStateEventEntityFields
import org.wormhole.android.sdk.internal.di.SessionDatabase
import org.wormhole.android.sdk.internal.query.process
import javax.inject.Inject

internal class StateEventDataSource @Inject constructor(@SessionDatabase private val monarchy: Monarchy,
                                                        private val realmSessionProvider: RealmSessionProvider) {

    fun getStateEvent(roomId: String, eventType: String, stateKey: QueryStringValue): Event? {
        return realmSessionProvider.withRealm { realm ->
            buildStateEventQuery(realm, roomId, setOf(eventType), stateKey).findFirst()?.root?.asDomain()
        }
    }

    fun getStateEventLive(roomId: String, eventType: String, stateKey: QueryStringValue): LiveData<Optional<Event>> {
        val liveData = monarchy.findAllMappedWithChanges(
                { realm -> buildStateEventQuery(realm, roomId, setOf(eventType), stateKey) },
                { it.root?.asDomain() }
        )
        return Transformations.map(liveData) { results ->
            results.firstOrNull().toOptional()
        }
    }

    fun getStateEvents(roomId: String, eventTypes: Set<String>, stateKey: QueryStringValue): List<Event> {
        return realmSessionProvider.withRealm { realm ->
            buildStateEventQuery(realm, roomId, eventTypes, stateKey)
                    .findAll()
                    .mapNotNull {
                        it.root?.asDomain()
                    }
        }
    }

    fun getStateEventsLive(roomId: String, eventTypes: Set<String>, stateKey: QueryStringValue): LiveData<List<Event>> {
        val liveData = monarchy.findAllMappedWithChanges(
                { realm -> buildStateEventQuery(realm, roomId, eventTypes, stateKey) },
                { it.root?.asDomain() }
        )
        return Transformations.map(liveData) { results ->
            results.filterNotNull()
        }
    }

    private fun buildStateEventQuery(realm: Realm,
                                     roomId: String,
                                     eventTypes: Set<String>,
                                     stateKey: QueryStringValue
    ): RealmQuery<CurrentStateEventEntity> {
        return realm.where<CurrentStateEventEntity>()
                .equalTo(CurrentStateEventEntityFields.ROOM_ID, roomId)
                .apply {
                    if (eventTypes.isNotEmpty()) {
                        `in`(CurrentStateEventEntityFields.TYPE, eventTypes.toTypedArray())
                    }
                }
                .process(CurrentStateEventEntityFields.STATE_KEY, stateKey)
    }
}
