package com.microsoft.walletlibrarydemo.feature.presentationlogic

import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId

interface ClickListener {
    fun listMatchingVerifiedIds(requirement: VerifiedIdRequirement)

    fun fulfillVerifiedIdRequirement(verifiedId: VerifiedId, requirement: VerifiedIdRequirement)
}