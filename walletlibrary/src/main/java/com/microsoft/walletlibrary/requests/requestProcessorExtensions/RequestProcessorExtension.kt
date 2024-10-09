package com.microsoft.walletlibrary.requests.requestProcessorExtensions

import com.microsoft.walletlibrary.requests.VerifiedIdPartialRequest
import com.microsoft.walletlibrary.requests.handlers.RequestProcessor
import kotlin.reflect.KClass

/**
 * Extension to an existing RequestProcessor to parse additional input fields for request parameters
 */
interface RequestProcessorExtension<T> {
    /**
     * Extension to the associated RequestProcessor's parsing
     * @param rawRequest Primitive form of the original request input
     * @param request RequestProcessor's base request to be updated
     * @return updated request with extension changes (if any)
     */
    abstract fun parse(rawRequest: T, request: VerifiedIdPartialRequest): VerifiedIdPartialRequest
}