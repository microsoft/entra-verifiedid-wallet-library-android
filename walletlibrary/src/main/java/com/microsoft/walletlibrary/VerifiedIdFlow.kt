package com.microsoft.walletlibrary

import android.content.Context
import com.microsoft.did.sdk.VerifiableCredentialSdk

class VerifiedIdFlow {

    fun init(context: Context): String {
        VerifiableCredentialSdk.init(context, "testingLibrary/1.0")
        return VerifiableCredentialSdk.issuanceService.getNonce()
    }
}