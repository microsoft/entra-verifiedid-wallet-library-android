// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

/**
 * Data model for a verified Verifiable Presentation
 */
interface PresentationVerified {
    // Redirect URI returned by presentation.
    val redirectUri: String?
}