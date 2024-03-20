/*---------------------------------------------------------------------------------------------
*  Copyright (c) Microsoft Corporation. All rights reserved.
*  Licensed under the MIT License. See License.txt in the project root for license information.
*--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary

import com.microsoft.walletlibrary.did.sdk.identifier.IdentifierManager
import com.microsoft.walletlibrary.util.WalletLibraryLogger

/**
 * Utilities such as logger, identityManager that are configured in builder and
 * all of library will use.
 */
class ExtensionConfiguration private constructor(
    /**
     * Logs and metrics class
     */
    val logger: WalletLibraryLogger,
    /**
     * Identifier manager for extension use
     */
    val identifierManager: ExtensionIdentifierManager
) {

    internal constructor(logger: WalletLibraryLogger, identifierManager: IdentifierManager) : this(
        logger,
        ExtensionIdentifierManager(identifierManager)
    )
}
