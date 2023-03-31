package com.microsoft.walletlibrary.util

import org.assertj.core.api.Assertions

class TestLogConsumer: WalletLibraryLogger.Consumer {
    override fun log(
        logLevel: WalletLibraryLogger.Level,
        message: String,
        throwable: Throwable?,
        tag: String
    ) {
        Assertions.assertThat(tag).isEqualTo("Test tag")
        when (logLevel) {
            WalletLibraryLogger.Level.INFO -> Assertions.assertThat(message).isEqualTo("Test info log message")
            WalletLibraryLogger.Level.WARN -> Assertions.assertThat(message).isEqualTo("Test warn log message")
            WalletLibraryLogger.Level.FAILURE -> Assertions.assertThat(message).isEqualTo("Test failure log message")
            WalletLibraryLogger.Level.ERROR -> Assertions.assertThat(message).isEqualTo("Test error log message")
            WalletLibraryLogger.Level.VERBOSE -> Assertions.assertThat(message).isEqualTo("Test verbose log message")
            WalletLibraryLogger.Level.DEBUG -> Assertions.assertThat(message).isEqualTo("Test debug log message")
        }
    }

    override fun event(name: String, properties: Map<String, String>?) {
        Assertions.assertThat(name).isEqualTo("Test event")
    }
}