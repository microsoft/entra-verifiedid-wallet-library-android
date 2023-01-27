package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.walletlibrary.requests.contract.RootOfTrust

class VerifiedIdClientBuilder(private val context: Context, private val verifiedIdClientInput: VerifiedIdClientInput) {

    fun build(): VerifiedIdClient {
        return VerifiedIdIssuanceClient(RootOfTrust(verifiedIdClientInput.resolve()))
    }
}