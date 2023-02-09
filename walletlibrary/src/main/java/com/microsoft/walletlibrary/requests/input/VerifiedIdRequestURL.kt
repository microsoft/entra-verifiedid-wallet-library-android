/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests.input

import android.net.Uri

/**
 * Determines and initiates a valid flow using the provided url string.
 */
class VerifiedIdRequestURL(internal val url: Uri): VerifiedIdRequestInput {
}