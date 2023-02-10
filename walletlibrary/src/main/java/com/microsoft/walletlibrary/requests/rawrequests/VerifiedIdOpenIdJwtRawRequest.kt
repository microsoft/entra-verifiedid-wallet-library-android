package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.Request

class VerifiedIdOpenIdJwtRawRequest(
    override val requestType: RequestType,
    override val rawRequest: Request
) : OpenIdRawRequest