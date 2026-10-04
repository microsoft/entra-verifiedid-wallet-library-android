// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary

/**
 * Provides a Boolean value when it is needed instead of when an object is constructed.
 */
fun interface BooleanProvider {
    fun get(): Boolean
}
