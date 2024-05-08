package com.microsoft.walletlibrary.util

object Constants {
    const val OPENID_SCHEME = "openid-vc"
    const val PURE_ISSUANCE_FLOW_VALUE = "create"
    const val IDTOKENHINT_CONFIGURATION = "https://self-issued.me"

    // Constants for Request fetching
    const val OPENID4VCI_INTER_OP_PROFILE = "oid4vci-interop-profile-version=0.0.1"
    const val REQUEST_URI = "request_uri"
    const val CREDENTIAL_OFFER_URI = "credential_offer_uri"
    const val OPENID4VCI_TYPE_HEADER = "openid4vci-proof+jwt"
    // https://www.rfc-editor.org/rfc/rfc7240
    const val PREFER_HEADER = "Prefer"
}
