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

package org.wormhole.android.sdk.internal.auth.registration

import org.wormhole.android.sdk.api.auth.registration.RegistrationAvailability
import org.wormhole.android.sdk.api.failure.Failure
import org.wormhole.android.sdk.api.failure.isRegistrationAvailabilityError
import org.wormhole.android.sdk.internal.auth.AuthAPI
import org.wormhole.android.sdk.internal.network.executeRequest
import org.wormhole.android.sdk.internal.task.Task

internal interface RegisterAvailableTask : Task<RegisterAvailableTask.Params, RegistrationAvailability> {
    data class Params(
            val userName: String
    )
}

internal class DefaultRegisterAvailableTask(private val authAPI: AuthAPI) : RegisterAvailableTask {
    override suspend fun execute(params: RegisterAvailableTask.Params): RegistrationAvailability {
        return try {
            executeRequest(null) {
                authAPI.registerAvailable(params.userName)
            }
            RegistrationAvailability.Available
        } catch (exception: Throwable) {
            if (exception.isRegistrationAvailabilityError()) {
                RegistrationAvailability.NotAvailable(exception as Failure.ServerError)
            } else {
                throw exception
            }
        }
    }
}
