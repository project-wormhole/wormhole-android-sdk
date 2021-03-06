/*
 * Copyright (c) 2020 The Matrix.org Foundation C.I.C.
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

package org.wormhole.android.sdk.internal.session.call

import org.wormhole.android.sdk.api.MatrixConfiguration
import org.wormhole.android.sdk.api.session.call.MxCall
import org.wormhole.android.sdk.api.session.room.model.call.CallCapabilities
import org.wormhole.android.sdk.api.session.room.model.call.CallInviteContent
import org.wormhole.android.sdk.api.util.Optional
import org.wormhole.android.sdk.internal.di.DeviceId
import org.wormhole.android.sdk.internal.di.UserId
import org.wormhole.android.sdk.internal.session.call.model.MxCallImpl
import org.wormhole.android.sdk.internal.session.profile.GetProfileInfoTask
import org.wormhole.android.sdk.internal.session.room.send.LocalEchoEventFactory
import org.wormhole.android.sdk.internal.session.room.send.queue.EventSenderProcessor
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

internal class MxCallFactory @Inject constructor(
        @DeviceId private val deviceId: String?,
        private val localEchoEventFactory: LocalEchoEventFactory,
        private val eventSenderProcessor: EventSenderProcessor,
        private val matrixConfiguration: MatrixConfiguration,
        private val getProfileInfoTask: GetProfileInfoTask,
        @UserId private val userId: String
) {

    fun createIncomingCall(roomId: String, opponentUserId: String, content: CallInviteContent): MxCall? {
        content.callId ?: return null
        return MxCallImpl(
                callId = content.callId,
                isOutgoing = false,
                roomId = roomId,
                userId = userId,
                ourPartyId = deviceId ?: "",
                opponentUserId = opponentUserId,
                isVideoCall = content.isVideo(),
                localEchoEventFactory = localEchoEventFactory,
                eventSenderProcessor = eventSenderProcessor,
                matrixConfiguration = matrixConfiguration,
                getProfileInfoTask = getProfileInfoTask
        ).apply {
            opponentPartyId = Optional.from(content.partyId)
            opponentVersion = content.version?.let { BigDecimal(it).intValueExact() } ?: MxCall.VOIP_PROTO_VERSION
            capabilities = content.capabilities ?: CallCapabilities()
        }
    }

    fun createOutgoingCall(roomId: String, opponentUserId: String, isVideoCall: Boolean): MxCall {
        return MxCallImpl(
                callId = UUID.randomUUID().toString(),
                isOutgoing = true,
                roomId = roomId,
                userId = userId,
                ourPartyId = deviceId ?: "",
                opponentUserId = opponentUserId,
                isVideoCall = isVideoCall,
                localEchoEventFactory = localEchoEventFactory,
                eventSenderProcessor = eventSenderProcessor,
                matrixConfiguration = matrixConfiguration,
                getProfileInfoTask = getProfileInfoTask
        )
    }
}
