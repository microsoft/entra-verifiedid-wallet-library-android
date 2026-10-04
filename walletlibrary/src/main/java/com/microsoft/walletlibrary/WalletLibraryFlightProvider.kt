// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary

/**
 * Features whose runtime state can be supplied by a Wallet Library consumer.
 */
enum class WalletLibraryFlight {
    UseLegacyDidResolver
}

/**
 * Supplies the current state of Wallet Library flights.
 *
 * Implementations are queried when flighted behavior runs so consumers can change flight state
 * without rebuilding [VerifiedIdClient].
 */
fun interface WalletLibraryFlightProvider {
    fun isEnabled(flight: WalletLibraryFlight): Boolean
}
