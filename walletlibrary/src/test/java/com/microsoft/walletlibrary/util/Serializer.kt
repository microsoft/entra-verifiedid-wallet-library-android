package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.ManifestIssuanceRequest
import com.microsoft.walletlibrary.requests.OpenIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdIssuanceRequest
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.requirements.AccessTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.IdTokenRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.requirements.SelfAttestedClaimRequirement
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.OpenIdVerifierStyle
import com.microsoft.walletlibrary.requests.styles.RequesterStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdManifestIssuerStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val defaultTestSerializer = Json {
    serializersModule = SerializersModule {
        polymorphic(VerifiedId::class) {
            subclass(VerifiableCredential::class)
        }
        polymorphic(VerifiedIdStyle::class) {
            subclass(BasicVerifiedIdStyle::class)
        }
        polymorphic(VerifiedIdRequest::class) {
            subclass(ManifestIssuanceRequest::class)
            subclass(OpenIdPresentationRequest::class)
        }
        polymorphic(VerifiedIdIssuanceRequest::class) {
            subclass(ManifestIssuanceRequest::class)
        }
        polymorphic(VerifiedIdPresentationRequest::class) {
            subclass(OpenIdPresentationRequest::class)
        }
        polymorphic(RequesterStyle::class) {
            subclass(VerifiedIdManifestIssuerStyle::class)
            subclass(OpenIdVerifierStyle::class)
        }
        polymorphic(Requirement::class) {
            subclass(AccessTokenRequirement::class)
            subclass(IdTokenRequirement::class)
            subclass(PinRequirement::class)
            subclass(SelfAttestedClaimRequirement::class)
            subclass(GroupRequirement::class)
            subclass(VerifiedIdRequirement::class)
        }
    }
    ignoreUnknownKeys = true
    isLenient = true
}