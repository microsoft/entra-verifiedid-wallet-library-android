package com.microsoft.walletlibrary.util

enum class VerifiedIdExceptions(val value: String) {
    NETWORKING_EXCEPTION("networking_error"),
    REQUIREMENT_NOT_MET_EXCEPTION("requirement_not_met"),
    MALFORMED_INPUT_EXCEPTION("malformed_input"),
    USER_CANCELED_EXCEPTION("user_canceled"),
    UNSPECIFIED_EXCEPTION("unspecified_error"),
    MALFORMED_CREDENTIAL_OFFER_EXCEPTION("malformed_credential_offer")
}