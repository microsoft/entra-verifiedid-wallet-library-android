package com.microsoft.walletlibrary.response

import com.microsoft.walletlibrary.requests.IssuanceRequest
import com.microsoft.walletlibrary.requests.Request
import com.microsoft.walletlibrary.requests.contract.Contract

sealed class Response(open val request: Request)

class IssuanceResponse(

    // Issuance Request associated with this response
    override val request: IssuanceRequest,

    // Associated contract for this issuance response
    val contract: Contract
) : Response(request)