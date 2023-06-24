package com.microsoft.walletlibrary.util

enum class VerifiedIdExceptions(val value: String) {
    NETWORKING_EXCEPTION("NetworkingException"),
    REQUIREMENT_NOT_MET_EXCEPTION("RequirementNotMet"),
    UNSPECIFIED_EXCEPTION("UnspecifiedException")
}

typealias VerifiedIdResult<T> = Result<T>