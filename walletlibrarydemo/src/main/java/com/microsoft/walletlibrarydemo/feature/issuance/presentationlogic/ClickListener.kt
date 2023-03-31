package com.microsoft.walletlibrarydemo.feature.issuance.presentationlogic

import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement

interface ClickListener {
    fun navigateToVerifiedId(requirement: VerifiedIdRequirement)

    fun fulfillVerifiedIdRequirement(verifiedId: com.microsoft.walletlibrarydemo.db.entities.VerifiedId, requirement: VerifiedIdRequirement)
}