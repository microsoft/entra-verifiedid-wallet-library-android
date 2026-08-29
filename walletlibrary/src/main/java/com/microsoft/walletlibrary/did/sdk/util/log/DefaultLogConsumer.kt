// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.util.log

import android.util.Log

internal class DefaultLogConsumer : SdkLog.Consumer {

    override fun log(logLevel: SdkLog.Level, message: String, throwable: Throwable?, tag: String) {
        if (throwable == null) {
            Log.println(getAndroidLogLevel(logLevel), tag, message)
        } else {
            Log.println(getAndroidLogLevel(logLevel), tag, message + "\n" + Log.getStackTraceString(throwable))
        }
    }

    override fun event(name: String, properties: Map<String, String>?) {
        properties?.entries?.joinToString(separator = "\n") { "${it.key}: ${it.value}" }?.let {
            Log.i(name, it)
        }
    }

    private fun getAndroidLogLevel(logLevel: SdkLog.Level): Int {
        return logLevel.severity() + 2
    }
}
