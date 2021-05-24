/*
 * Copyright 2021 The Matrix.org Foundation C.I.C.
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

package org.wormhole.android.sdk.internal.network

import org.wormhole.android.sdk.api.failure.GlobalError
import org.wormhole.android.sdk.internal.auth.SessionParamsStore
import org.wormhole.android.sdk.internal.di.SessionId
import org.wormhole.android.sdk.internal.session.SessionScope
import org.wormhole.android.sdk.internal.task.TaskExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SessionScope
internal class GlobalErrorHandler @Inject constructor(
        private val taskExecutor: TaskExecutor,
        private val sessionParamsStore: SessionParamsStore,
        @SessionId private val sessionId: String
) : GlobalErrorReceiver {

    var listener: Listener? = null

    override fun handleGlobalError(globalError: GlobalError) {
        Timber.e("Global error received: $globalError")

        if (globalError is GlobalError.InvalidToken && globalError.softLogout) {
            // Mark the token has invalid
            taskExecutor.executorScope.launch(Dispatchers.IO) {
                sessionParamsStore.setTokenInvalid(sessionId)
            }
        }

        listener?.onGlobalError(globalError)
    }

    internal interface Listener {
        fun onGlobalError(globalError: GlobalError)
    }
}
