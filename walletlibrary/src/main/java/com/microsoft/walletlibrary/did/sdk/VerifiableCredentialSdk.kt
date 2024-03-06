/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk

import android.content.Context
import androidx.preference.PreferenceManager
import com.microsoft.walletlibrary.did.sdk.di.DaggerSdkComponent
import com.microsoft.walletlibrary.did.sdk.util.DifWordList
import com.microsoft.walletlibrary.did.sdk.util.log.DefaultLogConsumer
import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog
import com.microsoft.walletlibrary.util.http.httpagent.IHttpAgent
import com.microsoft.walletlibrary.util.http.httpagent.OkHttpAgent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * This class initializes the VerifiableCredentialSdk. The `init` method has to be called before the members can be accessed.
 * Call the init method as soon as possible, for example in the `onCreate()` method of your `Application` implementation.
 * An Android context and user agent information (i.e, name/version) have to be provided as such:
 *
 * VerifiableCredentialSdk.init(getApplicationContext(), "");
 *
 * The `VerifiableCredentialManager` can be accessed through this static reference, but ideally should be provided
 * by your own dependency injection library. In the case of Dagger2 as such:
 *
 * @Provides
 * fun provideIssuanceService(): IssuanceService {
 *     return VerifiableCredentialSdk.issuanceService
 * }
 */
internal object VerifiableCredentialSdk {

    @JvmStatic
    internal lateinit var issuanceService: IssuanceService

    @JvmStatic
    internal lateinit var presentationService: PresentationService

    @JvmStatic
    internal lateinit var revocationService: RevocationService

    @JvmStatic
    internal lateinit var correlationVectorService: CorrelationVectorService

    @JvmStatic
    internal lateinit var backupService: BackupService

    @JvmStatic
    internal lateinit var identifierService: IdentifierService

    /**
     * Initializes VerifiableCredentialSdk
     *
     * @param context context instance
     * @param userAgentInfo it contains name and version of the client. It will be used in User-Agent header for all the requests.
     * @param logConsumer logger implementation to be used
     * @param polymorphicJsonSerializers serializer module
     * @param registrationUrl url used to register DID
     * @param resolverUrl url used to resolve DID
     * @param walletLibraryVersionInfo version of the library in use
     * @param interceptors HttpInterceptor to modify http request
     */
    // TODO(Change how version numbers are passed for headers when HTTP client layer is refactored)
    @JvmOverloads
    @JvmStatic
    internal fun init(
        context: Context,
        userAgentInfo: String = "",
        logConsumer: SdkLog.Consumer = DefaultLogConsumer(),
        polymorphicJsonSerializers: SerializersModule = Json.serializersModule,
        registrationUrl: String = "",
        resolverUrl: String = "https://discover.did.msidentity.com/v1.0/identifiers",
        walletLibraryVersionInfo: String = "",
        httpAgent: IHttpAgent = OkHttpAgent()
    ) {
        correlationVectorService = CorrelationVectorService( PreferenceManager.getDefaultSharedPreferences(context) )
        val sdkComponent = DaggerSdkComponent.builder()
            .context(context)
            .userAgentInfo(userAgentInfo)
            .walletLibraryVersionInfo(walletLibraryVersionInfo)
            .httpAgent(httpAgent)
            .registrationUrl(registrationUrl)
            .resolverUrl(resolverUrl)
            .polymorphicJsonSerializer(polymorphicJsonSerializers)
            .httpInterceptors(interceptors)
            .build()

        issuanceService = sdkComponent.issuanceService()
        presentationService = sdkComponent.presentationService()
        revocationService = sdkComponent.revocationService()
        correlationVectorService = sdkComponent.correlationVectorService()
        identifierService = sdkComponent.identifierManager()
        backupService = sdkComponent.backupAndRestoreService()

        correlationVectorService.startNewFlowAndSave()

        SdkLog.addConsumer(logConsumer)

        DifWordList.initialize(context)
    }
}