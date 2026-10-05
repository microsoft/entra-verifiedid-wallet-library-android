package com.microsoft.walletlibrary.util

class WalletLibraryEventRecorder : WalletLibraryLogger.Consumer {
    val events = mutableListOf<Event>()

    override fun log(
        logLevel: WalletLibraryLogger.Level,
        message: String,
        throwable: Throwable?,
        tag: String
    ) = Unit

    override fun event(name: String, properties: Map<String, String>?) {
        events.add(Event(name, properties))
    }

    data class Event(
        val name: String,
        val properties: Map<String, String>?
    )
}
