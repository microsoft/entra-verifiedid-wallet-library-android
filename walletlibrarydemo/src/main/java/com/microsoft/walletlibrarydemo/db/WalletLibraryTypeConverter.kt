package com.microsoft.walletlibrarydemo.db

import androidx.room.TypeConverter
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.walletlibrary.requests.styles.BasicVerifiedIdStyle
import com.microsoft.walletlibrary.requests.styles.VerifiedIdStyle
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
            polymorphic(VerifiedIdStyle::class) {
                subclass(BasicVerifiedIdStyle::class)
            }
        }
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    @JvmStatic
    fun dateToString(date: Date) =
        serializer.encodeToString(DateSerializer, date)

    @TypeConverter
    @JvmStatic
    fun stringToDate(string: String) =
        serializer.decodeFromString(DateSerializer, string)

    @TypeConverter
    @JvmStatic
    fun listToString(list: List<String>) =
        serializer.encodeToString(list)

    @TypeConverter
    @JvmStatic
    fun stringToList(string: String) =
        serializer.decodeFromString<List<String>>(string)

/*    @TypeConverter
    @JvmStatic
    fun styleToString(verifiedIdStyle: VerifiedIdStyle) =
        serializer.encodeToString(VerifiedIdStyle.serializer(), verifiedIdStyle)

    @TypeConverter
    @JvmStatic
    fun stringToStyle(string: String) =
        serializer.decodeFromString(VerifiedIdStyle.serializer(), string)*/

    @TypeConverter
    @JvmStatic
    fun contractToString(verifiableCredentialContract: VerifiableCredentialContract) =
        serializer.encodeToString(VerifiableCredentialContract.serializer(), verifiableCredentialContract)

    @TypeConverter
    @JvmStatic
    fun stringToContract(string: String) =
        serializer.decodeFromString(VerifiableCredentialContract.serializer(), string)

    @TypeConverter
    @JvmStatic
    fun vcInSdkToString(verifiableCredentialCredential: com.microsoft.did.sdk.credential.models.VerifiableCredential) =
        serializer.encodeToString(com.microsoft.did.sdk.credential.models.VerifiableCredential.serializer(), verifiableCredentialCredential)

    @TypeConverter
    @JvmStatic
    fun stringToVcInSdk(string: String) =
        serializer.decodeFromString(com.microsoft.did.sdk.credential.models.VerifiableCredential.serializer(), string)
}