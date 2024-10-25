package com.microsoft.walletlibrarydemo.extension

import com.microsoft.walletlibrary.ExtensionConfiguration
import com.microsoft.walletlibrary.requests.VerifiedIdExtension
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension

class ExampleExtension : VerifiedIdExtension {
    /**
     * List of prefer header values to be sent when resolving requests indicating extension support.
     */
    override val prefer: List<String>
        get() = listOf("wallet-library-extension=0.0.1")

    /**
     * List of RequestProcessorExtension to be injected into RequestProcessors
     */
    override fun createRequestProcessorExtensions(configuration: ExtensionConfiguration): List<RequestProcessorExtension<*>> {
        return listOf(ExampleRequestProcessorExtension())
    }
}