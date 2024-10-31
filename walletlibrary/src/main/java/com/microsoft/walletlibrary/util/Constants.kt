package com.microsoft.walletlibrary.util

object Constants {
    const val OPENID_SCHEME = "openid-vc"
    const val PURE_ISSUANCE_FLOW_VALUE = "create"
    const val IDTOKENHINT_CONFIGURATION = "https://self-issued.me"

    // Constants for Request fetching
    const val REQUEST_URI = "request_uri"
    const val CREDENTIAL_OFFER_URI = "credential_offer_uri"

    // https://www.rfc-editor.org/rfc/rfc7240
    const val PREFER_HEADER = "Prefer"
    // Constants for Request completion
    const val OPENID4VCI_TYPE_HEADER = "openid4vci-proof+jwt"
    // Constant Prefer header for requesting SiopV2 format
    const val SELF_ISSUED_OPENID_V2_PROFILE = "JWT-interop-profile-0.0.1"
    // Constant Prefer header for requesting OpenID4VCI format
    const val OPENID4VCI_INTER_OP_PROFILE = "oid4vci-interop-profile-version=0.0.1"
}
