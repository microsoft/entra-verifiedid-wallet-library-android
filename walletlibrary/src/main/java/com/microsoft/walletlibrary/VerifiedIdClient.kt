package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.requests.contract.RootOfTrust

interface VerifiedIdClient {
    val rootOfTrust: RootOfTrust
}