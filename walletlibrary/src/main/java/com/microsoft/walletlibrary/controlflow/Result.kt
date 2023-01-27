package com.microsoft.walletlibrary.controlflow

sealed class Result<out S> {
    class Success<out S>(val payload: S) : Result<S>()
    class Failure(val payload: WalletLibraryException) : Result<Nothing>()
}