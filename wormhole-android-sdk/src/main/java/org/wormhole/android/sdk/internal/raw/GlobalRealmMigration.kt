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

package org.wormhole.android.sdk.internal.raw

import io.realm.DynamicRealm
import io.realm.RealmMigration
import org.wormhole.android.sdk.internal.database.model.KnownServerUrlEntityFields
import timber.log.Timber

internal object GlobalRealmMigration : RealmMigration {

    // Current schema version
    const val SCHEMA_VERSION = 1L

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Timber.d("Migrating Auth Realm from $oldVersion to $newVersion")

        if (oldVersion <= 0) migrateTo1(realm)
    }

    private fun migrateTo1(realm: DynamicRealm) {
        realm.schema.create("KnownServerUrlEntity")
                .addField(KnownServerUrlEntityFields.URL, String::class.java)
                .addPrimaryKey(KnownServerUrlEntityFields.URL)
                .setRequired(KnownServerUrlEntityFields.URL, true)
    }
}
