package com.microsoft.walletlibrary.response

import com.microsoft.walletlibrary.requests.IssuanceRequest
import com.microsoft.walletlibrary.requests.Request
import com.microsoft.walletlibrary.requests.contract.Contract

sealed class Response(open val request: Request)

class IssuanceResponse(override val request: IssuanceRequest, val contract: Contract) : Response(request)