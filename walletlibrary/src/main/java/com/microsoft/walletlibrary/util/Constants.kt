package com.microsoft.walletlibrary.util

object Constants {
    const val OPENID_SCHEME = "openid-vc"
    const val PURE_ISSUANCE_FLOW_VALUE = "create"
    const val IDTOKENHINT_CONFIGURATION = "https://self-issued.me"

    // Constants for Request fetching
    const val REQUEST_URI = "request_uri"
    const val CREDENTIAL_OFFER_URI = "credential_offer_uri"

    // Constants for Request completion
    const val OPENID4VCI_TYPE_HEADER = "openid4vci-proof+jwt"
}
