package com.microsoft.walletlibrary.util

enum class VerifiedIdExceptions(val value: String) {
    NETWORKING_EXCEPTION("networking_error"),
    REQUIREMENT_NOT_MET_EXCEPTION("requirement_not_met"),
    MALFORMED_INPUT_EXCEPTION("malformed_input"),
    USER_CANCELED_EXCEPTION("user_canceled"),
    UNSPECIFIED_EXCEPTION("unspecified_error"),
    CREDENTIAL_OFFER_FETCH_EXCEPTION("credential_offer_fetch_error"),
    CREDENTIAL_METADATA_FETCH_EXCEPTION("credential_metadata_fetch_error"),
    OPENID_WELL_KNOWN_CONFIG_FETCH_EXCEPTION("openid_well_known_config_fetch_error"),
    MALFORMED_CREDENTIAL_OFFER_EXCEPTION("malformed_credential_offer"),
    MALFORMED_CREDENTIAL_METADATA_EXCEPTION("malformed_credential_metadata"),
    MALFORMED_SIGNED_METADATA_EXCEPTION("malformed_signed_metadata"),
    INVALID_SIGNATURE_EXCEPTION("invalid_signature"),
    INVALID_PROPERTY_EXCEPTION("invalid_property"),
    REQUEST_CREATION_EXCEPTION("request_creation_error"),
    REQUEST_SEND_EXCEPTION("request_send_error")
}