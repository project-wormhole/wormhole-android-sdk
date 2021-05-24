/*
 * Copyright (c) 2021 New Vector Ltd
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

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.wormhole.android.sdk.internal.di.MatrixScope
import org.wormhole.android.sdk.wormhole.WormholeConfigProvider
import javax.inject.Inject

@MatrixScope
class WormholeInterceptor @Inject constructor()
    : Interceptor {

    @Inject lateinit var wormholeConfigProvider: WormholeConfigProvider

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request().newBuilder()
//                .addHeader("appid", wormholeConfigProvider.appId)
//                .addHeader("access_token", wormholeConfigProvider.accessToken)
                .build()
        return chain.proceed(request)
    }
}
