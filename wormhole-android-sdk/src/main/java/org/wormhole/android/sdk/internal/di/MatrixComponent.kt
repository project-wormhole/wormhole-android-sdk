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

package org.wormhole.android.sdk.internal.di

import android.content.Context
import android.content.res.Resources
import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import org.wormhole.android.sdk.api.Wormhole
import org.wormhole.android.sdk.api.MatrixConfiguration
import org.wormhole.android.sdk.api.auth.AuthenticationService
import org.wormhole.android.sdk.api.auth.HomeServerHistoryService
import org.wormhole.android.sdk.api.raw.RawService
import org.wormhole.android.sdk.internal.SessionManager
import org.wormhole.android.sdk.internal.auth.AuthModule
import org.wormhole.android.sdk.internal.auth.SessionParamsStore
import org.wormhole.android.sdk.internal.raw.RawModule
import org.wormhole.android.sdk.internal.session.MockHttpInterceptor
import org.wormhole.android.sdk.internal.session.TestInterceptor
import org.wormhole.android.sdk.internal.task.TaskExecutor
import org.wormhole.android.sdk.internal.util.BackgroundDetectionObserver
import org.wormhole.android.sdk.internal.util.MatrixCoroutineDispatchers
import org.matrix.olm.OlmManager
import java.io.File

@Component(modules = [
    MatrixModule::class,
    NetworkModule::class,
    AuthModule::class,
    RawModule::class,
    NoOpTestModule::class
])
@MatrixScope
internal interface MatrixComponent {

    fun matrixCoroutineDispatchers(): MatrixCoroutineDispatchers

    fun moshi(): Moshi

    @Unauthenticated
    fun okHttpClient(): OkHttpClient

    @MockHttpInterceptor
    fun testInterceptor(): TestInterceptor?

    fun authenticationService(): AuthenticationService

    fun rawService(): RawService

    fun homeServerHistoryService(): HomeServerHistoryService

    fun context(): Context

    fun matrixConfiguration(): MatrixConfiguration

    fun resources(): Resources

    @CacheDirectory
    fun cacheDir(): File

    fun olmManager(): OlmManager

    fun taskExecutor(): TaskExecutor

    fun sessionParamsStore(): SessionParamsStore

    fun backgroundDetectionObserver(): BackgroundDetectionObserver

    fun sessionManager(): SessionManager

    fun inject(wormhole: Wormhole)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context,
                   @BindsInstance matrixConfiguration: MatrixConfiguration): MatrixComponent
    }
}
