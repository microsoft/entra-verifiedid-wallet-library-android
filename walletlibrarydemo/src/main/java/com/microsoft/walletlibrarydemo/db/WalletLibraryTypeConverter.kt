package com.microsoft.walletlibrarydemo.db

import androidx.room.TypeConverter
import com.microsoft.walletlibrary.verifiedid.DateSerializer
import com.microsoft.walletlibrary.verifiedid.VerifiableCredential
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.*

object WalletLibraryTypeConverter {

    private val serializer: Json = Json {
        serializersModule = SerializersModule {
            polymorphic(VerifiedId::class) {
                subclass(VerifiableCredential::class)
            }
        }
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    @JvmStatic
    fun verifiableCredentialToString(verifiableCredential: VerifiableCredential) = serializer.encodeToString(
        VerifiableCredential.serializer(), verifiableCredential)

    @TypeConverter
    @JvmStatic
    fun stringToVerifiableCredential(serializedVc: String) =
        serializer.decodeFromString(VerifiableCredential.serializer(), serializedVc)

    @TypeConverter
    @JvmStatic
    fun vCInSdkToString(verifiableCredential: com.microsoft.did.sdk.credential.models.VerifiableCredential) = serializer.encodeToString(
        com.microsoft.did.sdk.credential.models.VerifiableCredential.serializer(), verifiableCredential)

    @TypeConverter
    @JvmStatic
    fun stringToVcInSdk(serializedVc: String) =
        serializer.decodeFromString(com.microsoft.did.sdk.credential.models.VerifiableCredential.serializer(), serializedVc)

    @TypeConverter
    @JvmStatic
    fun vCContractInSdkToString(verifiableCredential: com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract) = serializer.encodeToString(
        com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract.serializer(), verifiableCredential)

    @TypeConverter
    @JvmStatic
    fun stringToVcContractInSdk(serializedVc: String) =
        serializer.decodeFromString(com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract.serializer(), serializedVc)

    @TypeConverter
    @JvmStatic
    fun vcContentsToString(verifiableCredentialContent: com.microsoft.did.sdk.credential.models.VerifiableCredentialContent) =
        serializer.encodeToString(com.microsoft.did.sdk.credential.models.VerifiableCredentialContent.serializer(), verifiableCredentialContent)

    @TypeConverter
    @JvmStatic
    fun stringToVcContents(serializedVcContent: String) =
        serializer.decodeFromString(com.microsoft.did.sdk.credential.models.VerifiableCredentialContent.serializer(), serializedVcContent)

    @TypeConverter
    @JvmStatic
    fun dateToString(date: Date) =
        serializer.encodeToString(DateSerializer, date)

    @TypeConverter
    @JvmStatic
    fun stringToDate(date: String) =
        serializer.decodeFromString(DateSerializer, date)

    @TypeConverter
    @JvmStatic
    fun listToString(list: List<String>) =
        serializer.encodeToString(list)

    @TypeConverter
    @JvmStatic
    fun stringToList(list: String) =
        serializer.decodeFromString<List<String>>(list)
}