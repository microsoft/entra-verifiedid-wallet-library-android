package com.microsoft.walletlibrarydemo.util

import android.util.Log
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import com.microsoft.walletlibrary.util.WalletLibraryLogger.Consumer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConsoleLogger: Consumer {
    override fun log(
        logLevel: WalletLibraryLogger.Level,
        message: String,
        throwable: Throwable?,
        tag: String
    ) {
        when (logLevel) {
            WalletLibraryLogger.Level.VERBOSE -> {
                Log.v(tag, message, throwable)
            }
            WalletLibraryLogger.Level.INFO -> {
                Log.i(tag, message, throwable)
            }
            WalletLibraryLogger.Level.WARN -> {
                Log.w(tag, message, throwable)
            }
            WalletLibraryLogger.Level.DEBUG -> {
                Log.d(tag, message, throwable)
            }
            WalletLibraryLogger.Level.ERROR -> {
                Log.e(tag, message, throwable)
            }
            WalletLibraryLogger.Level.FAILURE -> {
                Log.wtf(tag, message, throwable)
            }
        }
    }

    override fun event(name: String, properties: Map<String, String>?) {
        Log.v("metric:$name", Json.encodeToString(properties))
    }

}