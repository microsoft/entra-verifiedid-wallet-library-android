// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class LinkedDomainResult

@Serializable
@SerialName("LinkedDomainVerified")
internal class LinkedDomainVerified(val domainUrl: String) : LinkedDomainResult()

@Serializable
@SerialName("LinkedDomainUnVerified")
internal class LinkedDomainUnVerified(val domainUrl: String) : LinkedDomainResult()

@Serializable
@SerialName("LinkedDomainMissing")
internal object LinkedDomainMissing : LinkedDomainResult()