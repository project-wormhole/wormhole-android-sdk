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

package org.wormhole.android.sdk.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.zhuinden.monarchy.Monarchy
import org.wormhole.android.sdk.BuildConfig
import org.wormhole.android.sdk.api.auth.AuthenticationService
import org.wormhole.android.sdk.api.auth.HomeServerHistoryService
import org.wormhole.android.sdk.api.legacy.LegacySessionImporter
import org.wormhole.android.sdk.api.network.ApiInterceptorListener
import org.wormhole.android.sdk.api.network.ApiPath
import org.wormhole.android.sdk.api.raw.RawService
import org.wormhole.android.sdk.common.DaggerTestMatrixComponent
import org.wormhole.android.sdk.internal.SessionManager
import org.wormhole.android.sdk.internal.network.ApiInterceptor
import org.wormhole.android.sdk.internal.network.UserAgentHolder
import org.wormhole.android.sdk.internal.util.BackgroundDetectionObserver
import org.matrix.olm.OlmManager
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * This is the main entry point to the matrix sdk.
 * To get the singleton instance, use getInstance static method.
 */
class Matrix private constructor(context: Context, matrixConfiguration: MatrixConfiguration) {

    @Inject internal lateinit var legacySessionImporter: LegacySessionImporter
    @Inject internal lateinit var authenticationService: AuthenticationService
    @Inject internal lateinit var rawService: RawService
    @Inject internal lateinit var userAgentHolder: UserAgentHolder
    @Inject internal lateinit var backgroundDetectionObserver: BackgroundDetectionObserver
    @Inject internal lateinit var olmManager: OlmManager
    @Inject internal lateinit var sessionManager: SessionManager
    @Inject internal lateinit var homeServerHistoryService: HomeServerHistoryService
    @Inject internal lateinit var apiInterceptor: ApiInterceptor

    private val uiHandler = Handler(Looper.getMainLooper())

    init {
        Monarchy.init(context)
        DaggerTestMatrixComponent.factory().create(context, matrixConfiguration).inject(this)
        if (context.applicationContext !is Configuration.Provider) {
            WorkManager.initialize(context, Configuration.Builder().setExecutor(Executors.newCachedThreadPool()).build())
        }
        uiHandler.post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(backgroundDetectionObserver)
        }
    }

    fun getUserAgent() = userAgentHolder.userAgent

    fun authenticationService(): AuthenticationService {
        return authenticationService
    }

    fun rawService() = rawService

    fun homeServerHistoryService() = homeServerHistoryService

    fun legacySessionImporter(): LegacySessionImporter {
        return legacySessionImporter
    }

    fun registerApiInterceptorListener(path: ApiPath, listener: ApiInterceptorListener) {
        apiInterceptor.addListener(path, listener)
    }

    fun unregisterApiInterceptorListener(path: ApiPath, listener: ApiInterceptorListener) {
        apiInterceptor.removeListener(path, listener)
    }

    companion object {

        private lateinit var instance: Matrix
        private val isInit = AtomicBoolean(false)

        private var wormholeDevMode: Boolean = false
        private var wormholeDomainStaging = "stage-api.wormholeim.org"
        private var wormholeDomainProduction = "api.wormholeim.org"
        private var wormholeAppId: String? = null
        private var wormholeAccessToken: String? = null

        fun initialize(context: Context, matrixConfiguration: MatrixConfiguration) {
            if (isInit.compareAndSet(false, true)) {
                instance = Matrix(context.applicationContext, matrixConfiguration)
                wormholeAppId = matrixConfiguration.wormholeAppId
                wormholeAccessToken = matrixConfiguration.wormholeAccessToken
                wormholeDevMode = matrixConfiguration.wormholeDevMode
            }
        }

        fun getInstance(context: Context): Matrix {
            if (isInit.compareAndSet(false, true)) {
                val appContext = context.applicationContext
                if (appContext is MatrixConfiguration.Provider) {
                    val matrixConfiguration = (appContext as MatrixConfiguration.Provider).providesMatrixConfiguration()
                    instance = Matrix(appContext, matrixConfiguration)
                } else {
                    throw IllegalStateException("Matrix is not initialized properly." +
                            " You should call Matrix.initialize or let your application implements MatrixConfiguration.Provider.")
                }
            }
            return instance
        }

        fun getSdkVersion(): String {
            return BuildConfig.VERSION_NAME + " (" + BuildConfig.GIT_SDK_REVISION + ")"
        }

        fun getWormholeApplicationId(): String {
            if (wormholeAppId != null) {
                return wormholeAppId!!
            }else{
                throw IllegalStateException("Wormhole is not initialized properly." +
                        " You should set wormhole application id!")
            }
        }

        fun getWormholeAccessToken(): String {
            if (wormholeAccessToken != null) {
                return wormholeAccessToken!!
            }else{
                throw IllegalStateException("Wormhole is not initialized properly." +
                        " You should set wormhole access token!")
            }
        }

        fun getWormholeDomain(): String {
            return if (wormholeDevMode) wormholeDomainStaging else wormholeDomainProduction
        }
    }
}
