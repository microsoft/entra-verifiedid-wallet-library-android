// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import kotlinx.serialization.Serializable

/**
 * Data model of a generic successfully completed operation.
 */
@Serializable(SuccessfulCompletionResultSerializer::class)
sealed interface SuccessfulCompletionResult