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

package org.wormhole.android.sdk.internal.auth.db

import org.wormhole.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.wormhole.android.sdk.internal.auth.login.ResetPasswordData
import org.wormhole.android.sdk.internal.auth.registration.ThreePidData
import java.util.UUID

/**
 * This class holds all pending data when creating a session, either by login or by register
 */
internal data class PendingSessionData(
        val homeServerConnectionConfig: HomeServerConnectionConfig,

        /* ==========================================================================================
         * Common
         * ========================================================================================== */

        val clientSecret: String = UUID.randomUUID().toString(),
        val sendAttempt: Int = 0,

        /* ==========================================================================================
         * For login
         * ========================================================================================== */

        val resetPasswordData: ResetPasswordData? = null,

        /* ==========================================================================================
         * For register
         * ========================================================================================== */

        val currentSession: String? = null,
        val isRegistrationStarted: Boolean = false,
        val currentThreePidData: ThreePidData? = null
)
