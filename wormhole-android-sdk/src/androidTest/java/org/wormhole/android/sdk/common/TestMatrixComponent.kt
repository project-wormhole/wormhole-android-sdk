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

package org.wormhole.android.sdk.common

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.wormhole.android.sdk.api.MatrixConfiguration
import org.wormhole.android.sdk.internal.auth.AuthModule
import org.wormhole.android.sdk.internal.di.MatrixComponent
import org.wormhole.android.sdk.internal.di.MatrixModule
import org.wormhole.android.sdk.internal.di.MatrixScope
import org.wormhole.android.sdk.internal.di.NetworkModule
import org.wormhole.android.sdk.internal.raw.RawModule

@Component(modules = [
    TestModule::class,
    MatrixModule::class,
    NetworkModule::class,
    AuthModule::class,
    RawModule::class,
    TestNetworkModule::class
])
@MatrixScope
internal interface TestMatrixComponent : MatrixComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context,
                   @BindsInstance matrixConfiguration: MatrixConfiguration): TestMatrixComponent
    }
}
