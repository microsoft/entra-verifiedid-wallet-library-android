package com.microsoft.walletlibrary.requests.requestProcessorExtensions

import com.microsoft.walletlibrary.requests.VerifiedIdPartialRequest
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import kotlin.reflect.KClass

/**
 * Extension to an existing RequestProcessor to parse additional input fields for request parameters
 */
interface RequestProcessorExtension {
    /**
     * Type of RequestProcessor this extension should be injected into
     */
    abstract val associatedRequestProcessor: KClass<out RequestProcessor>

    /**
     * Extension to the associated RequestProcessor's parsing
     * @param rawRequest Primitive form of the original request input
     * @param request RequestProcessor's base request to be updated
     * @return updated request with extension changes (if any)
     */
    abstract fun parse(rawRequest: Any, request: VerifiedIdPartialRequest): VerifiedIdPartialRequest
}