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

package org.wormhole.android.sdk.internal.crypto.keysbackup.model

import org.wormhole.android.sdk.internal.crypto.model.CryptoDeviceInfo

/**
 * A signature in a the `KeyBackupVersionTrust` object.
 */
class KeyBackupVersionTrustSignature {

    /**
     * The device that signed the backup version.
     */
    var device: CryptoDeviceInfo? = null

    /**
     *Flag to indicate the signature from this device is valid.
     */
    var valid = false
}
