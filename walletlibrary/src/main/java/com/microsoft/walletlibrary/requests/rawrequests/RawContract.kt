package com.microsoft.walletlibrary.requests.rawrequests

import com.microsoft.did.sdk.credential.service.IssuanceRequest

class RawContract(
    override val rawRequest: IssuanceRequest,
    override val requestType: RequestType
): RawRequest