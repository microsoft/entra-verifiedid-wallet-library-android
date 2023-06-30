package com.microsoft.walletlibrary.util

enum class VerifiedIdExceptions(val value: String) {
    NETWORKING_EXCEPTION("networking_error"),
    REQUIREMENT_NOT_MET_EXCEPTION("requirement_not_met"),
    UNSPECIFIED_EXCEPTION("unspecified_error")
}