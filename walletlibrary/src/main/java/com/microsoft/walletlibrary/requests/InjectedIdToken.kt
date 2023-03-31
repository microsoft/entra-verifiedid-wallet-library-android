package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requirements.PinRequirement

internal class InjectedIdToken(internal val rawToken: String, internal val pinRequirement: PinRequirement?)