package com.microsoft.walletlibrary.requests

import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension

/**
 * Interface to implement for injecting and extending Wallet Library capabilities
 */
interface VerifiedIdExtension {
    /**
     * List of prefer header values to be sent when resolving requests indicating extension support.
     */
    val prefer: List<String>

    /**
     * List of RequestProcessorExtension to be injected into RequestProcessors
     */
    val requestProcessorExtensions: List<RequestProcessorExtension>?
}