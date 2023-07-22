package com.microsoft.walletlibrary.did.sdk.datasource.network.interceptors

import com.microsoft.walletlibrary.did.sdk.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class WalletLibraryHeaderInterceptor(private val walletLibraryVersion: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithWalletLibraryVersionInfo = originalRequest.newBuilder()
            .header(Constants.WALLET_LIBRARY_VERSION_HEADER, walletLibraryVersion)
            .build()
        return chain.proceed(requestWithWalletLibraryVersionInfo)
    }
}