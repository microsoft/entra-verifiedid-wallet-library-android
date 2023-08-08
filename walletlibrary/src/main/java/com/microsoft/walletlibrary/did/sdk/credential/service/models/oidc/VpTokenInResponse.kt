// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.credential.service.models.oidc

import com.microsoft.walletlibrary.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VpTokenInResponse(

    @SerialName("presentation_submission")
    val presentationSubmission: PresentationSubmission
)
