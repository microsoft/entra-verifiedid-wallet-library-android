// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.verifiedid

import com.microsoft.walletlibrary.networking.entities.EmptyResponse
import com.microsoft.walletlibrary.networking.entities.ImplicitAuthenticationResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

internal object SuccessfulCompletionResultSerializer : JsonContentPolymorphicSerializer<SuccessfulCompletionResult>(SuccessfulCompletionResult::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out SuccessfulCompletionResult> {
        val json = element.jsonObject

        if (json.containsKey(ImplicitAuthenticationResponse.REDIRECT_URI_KEY_NAME)) {
            return ImplicitAuthenticationResponse.serializer()
        }
        return EmptyResponse.serializer()
    }
}