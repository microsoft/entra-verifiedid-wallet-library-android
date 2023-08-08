package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.did.sdk.util.log.SdkLog

internal class WalletLibraryVCSDKLogConsumer(private val logger: WalletLibraryLogger): SdkLog.Consumer {

    override fun log(logLevel: SdkLog.Level, message: String, throwable: Throwable?, tag: String) {
        logger.log(mapVcLogLevelToWalletLibraryLogLevel(logLevel), message, throwable, tag)
    }

    override fun event(name: String, properties: Map<String, String>?) {
        logger.event(name, properties)
    }

    private fun mapVcLogLevelToWalletLibraryLogLevel(logLevel: SdkLog.Level): WalletLibraryLogger.Level {
        return when(logLevel) {
            SdkLog.Level.VERBOSE -> WalletLibraryLogger.Level.VERBOSE
            SdkLog.Level.DEBUG -> WalletLibraryLogger.Level.DEBUG
            SdkLog.Level.INFO -> WalletLibraryLogger.Level.INFO
            SdkLog.Level.WARN -> WalletLibraryLogger.Level.WARN
            SdkLog.Level.ERROR -> WalletLibraryLogger.Level.ERROR
            SdkLog.Level.FAILURE -> WalletLibraryLogger.Level.FAILURE
        }
    }
}