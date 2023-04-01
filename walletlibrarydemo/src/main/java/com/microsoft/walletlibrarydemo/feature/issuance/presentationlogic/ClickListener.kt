package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId

interface ClickListener {
    fun navigateToVerifiedIds(requirement: VerifiedIdRequirement)

    fun fulfillVerifiedIdRequirement(verifiedId: VerifiedId, requirement: VerifiedIdRequirement)
}