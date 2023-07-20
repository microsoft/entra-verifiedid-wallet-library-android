// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.internal

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FeatureFlag @Inject constructor() {
    var linkedDomains: Boolean = true
}