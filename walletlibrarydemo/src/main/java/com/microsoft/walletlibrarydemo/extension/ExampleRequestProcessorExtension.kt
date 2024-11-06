package com.microsoft.walletlibrarydemo.extension

import com.microsoft.walletlibrary.requests.VerifiedIdPartialRequest
import com.microsoft.walletlibrary.requests.handlers.OpenIdRequestProcessor
import com.microsoft.walletlibrary.requests.requestProcessorExtensions.RequestProcessorExtension
import com.microsoft.walletlibrary.requests.rawrequests.OpenIdRawRequest
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.GroupRequirementOperator

class ExampleRequestProcessorExtension() : RequestProcessorExtension<OpenIdRawRequest> {
    override val associatedRequestProcessor = OpenIdRequestProcessor::class

    /**
     * Extension to the associated RequestProcessor's parsing
     * @param rawRequest Primitive form of the original request input
     * @param request RequestProcessor's base request to be updated
     * @return updated request with extension changes (if any)
     */
    override fun parse(
        rawRequest: OpenIdRawRequest,
        request: VerifiedIdPartialRequest
    ): VerifiedIdPartialRequest {
        if (request.requirement is GroupRequirement) {
            val requirement = request.requirement as GroupRequirement
            requirement.requirements.add(ExtensionRequirement("Hello, World!"))
        } else {
            val groupRequirement = GroupRequirement(
                true,
                mutableListOf(request.requirement,
                    ExtensionRequirement("Hello, World!")),
                GroupRequirementOperator.ALL
            )
            request.requirement = groupRequirement
        }
        return request
    }
}