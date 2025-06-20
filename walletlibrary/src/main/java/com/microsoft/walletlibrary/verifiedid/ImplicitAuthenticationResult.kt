// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

/**
 * Data model of an implicit authentication response (e.g., from a Verified Presentation submission).
 */
interface ImplicitAuthenticationResult : SuccessfulCompletionResult {
    // Redirect URI returned by presentation.
    val redirectUri: String
}