/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.walletlibrary.did.sdk.di

import android.content.Context
import com.microsoft.walletlibrary.did.sdk.BackupService
import com.microsoft.walletlibrary.did.sdk.CorrelationVectorService
import com.microsoft.walletlibrary.did.sdk.IdentifierService
import com.microsoft.walletlibrary.did.sdk.IssuanceService
import com.microsoft.walletlibrary.did.sdk.LinkedDomainsService
import com.microsoft.walletlibrary.did.sdk.PresentationService
import com.microsoft.walletlibrary.did.sdk.RevocationService
import com.microsoft.walletlibrary.interceptor.HttpInterceptor
import dagger.BindsInstance
import dagger.Component
import kotlinx.serialization.modules.SerializersModule
import javax.inject.Named
import javax.inject.Singleton

/**
 * This interface is used by Dagger to generate the code in `DaggerSdkComponent`. It exposes the dependency graph to
 * the outside. Dagger will expose the type inferred by the return type of the interface function.
 *
 * More information:
 * https://dagger.dev/users-guide
 * https://developer.android.com/training/dependency-injection
 */
@Singleton
@Component(modules = [SdkModule::class])
internal interface SdkComponent {

    fun identifierManager(): IdentifierService

    fun issuanceService(): IssuanceService

    fun presentationService(): PresentationService

    fun revocationService(): RevocationService

    fun linkedDomainsService(): LinkedDomainsService

    fun correlationVectorService(): CorrelationVectorService

    fun backupAndRestoreService(): BackupService

    @Component.Builder
    interface Builder {
        fun build(): SdkComponent

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun resolverUrl(@Named("resolverUrl") resolverUrl: String): Builder

        @BindsInstance
        fun registrationUrl(@Named("registrationUrl") registrationUrl: String): Builder

        @BindsInstance
        fun userAgentInfo(@Named("userAgentInfo") userAgentInfo: String): Builder

        @BindsInstance
        fun walletLibraryVersionInfo(@Named("walletLibraryVersionInfo") walletLibraryVersionInfo: String): Builder

        @BindsInstance
        fun polymorphicJsonSerializer(@Named("polymorphicJsonSerializer") jsonSerializer: SerializersModule): Builder

        @BindsInstance
        fun httpInterceptors(@Named("httpInterceptors") httpInterceptors: List<HttpInterceptor>): Builder
    }
}