package com.microsoft.walletlibrary.util

import org.junit.BeforeClass
import org.junit.Test

internal class WalletLibraryLoggerTest {
    companion object {
        private val walletLibraryLogger: WalletLibraryLogger = WalletLibraryLogger

        @BeforeClass
        @JvmStatic
        fun setUp() {
            walletLibraryLogger.CONSUMERS.add(TestLogConsumer())
        }
    }

    @Test
    fun testInfo_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.i("Test info log message", tag = "Test tag")
    }

    @Test
    fun testWarn_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.w("Test warn log message", tag = "Test tag")
    }

    @Test
    fun testDebug_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.d("Test debug log message", tag = "Test tag")
    }

    @Test
    fun testError_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.e("Test error log message", tag = "Test tag")
    }

    @Test
    fun testFailure_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.f("Test failure log message", tag = "Test tag")
    }

    @Test
    fun testVerbose_WithTestConsumer_LogsSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.v("Test verbose log message", tag = "Test tag")
    }

    @Test
    fun testEvent_WithTestConsumer_SendsEventSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.event("Test event")
    }

    @Test
    fun testLog_InfoLevelWithTestConsumer_SendsEventSuccessfullyUsingTestConsumer() {
        walletLibraryLogger.log(WalletLibraryLogger.Level.INFO, "Test info log message", tag = "Test tag")
    }
}