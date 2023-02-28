/**---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.Requirement
import com.microsoft.walletlibrary.requests.styles.RequesterStyle

/**
 * Contents in a Verified Id Request.
 * It is used to map protocol specific requests in SDK to abstract request objects in library.
 */
internal class VerifiedIdRequestContent(
    // Attributes describing the requester (eg. name, logo).
    internal val requesterStyle: RequesterStyle,

    // Information describing the requirements needed to complete the flow.
    internal val requirement: Requirement,

    // Root of trust of the requester (eg. linked domains).
    internal val rootOfTrust: RootOfTrust
)