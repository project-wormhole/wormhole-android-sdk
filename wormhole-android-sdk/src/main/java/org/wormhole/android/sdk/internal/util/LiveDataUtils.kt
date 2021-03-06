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

package org.wormhole.android.sdk.internal.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

object LiveDataUtils {

    fun <FIRST, SECOND, OUT> combine(firstSource: LiveData<FIRST>,
                                     secondSource: LiveData<SECOND>,
                                     mapper: (FIRST, SECOND) -> OUT): LiveData<OUT> {
        return MediatorLiveData<OUT>().apply {
            var firstValue: FIRST? = null
            var secondValue: SECOND? = null

            val valueDispatcher = {
                firstValue?.let { safeFirst ->
                    secondValue?.let { safeSecond ->
                        val mappedValue = mapper(safeFirst, safeSecond)
                        postValue(mappedValue)
                    }
                }
            }

            addSource(firstSource) {
                firstValue = it
                valueDispatcher()
            }

            addSource(secondSource) {
                secondValue = it
                valueDispatcher()
            }
        }
    }
}
