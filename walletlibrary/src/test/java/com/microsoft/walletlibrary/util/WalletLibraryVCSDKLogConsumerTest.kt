package com.microsoft.walletlibrary.util

import com.microsoft.did.sdk.util.log.SdkLog
import org.junit.BeforeClass
import org.junit.Test

internal class WalletLibraryVCSDKLogConsumerTest {

    companion object {
        private val walletLibraryLogger: WalletLibraryLogger = WalletLibraryLogger
        private val walletLibraryVCSDKLogConsumer: WalletLibraryVCSDKLogConsumer =
            WalletLibraryVCSDKLogConsumer(walletLibraryLogger)

        @BeforeClass
        @JvmStatic
        fun setUp() {
            walletLibraryLogger.CONSUMERS.add(TestLogConsumer())
        }
    }

    @Test
    fun testLog_VcSDKInfoLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.INFO,
            "Test info log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testLog_VcSDKWarnLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.WARN,
            "Test warn log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testLog_VcSDKDebugLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.DEBUG,
            "Test debug log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testLog_VcSDKFailureLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.FAILURE,
            "Test failure log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testLog_VcSDKVerboseLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.VERBOSE,
            "Test verbose log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testLog_VcSDKErrorLevel_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.log(
            SdkLog.Level.ERROR,
            "Test error log message",
            null,
            "Test tag"
        )
    }

    @Test
    fun testEvent_NamePassed_LogsInWalletLibraryLogger() {
        walletLibraryVCSDKLogConsumer.event("Test event", null)
    }
}