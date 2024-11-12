// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.did.sdk.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.microsoft.walletlibrary.datasource.db.dao.HolderIdentifierDataDao
import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierData
import com.microsoft.walletlibrary.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.walletlibrary.did.sdk.identifier.models.Identifier

/**
 * Abstract description of the database interface that is supposed to be provided by Room. New entities have to be
 * added here, so that the schema includes them.
 *
 * Whenever the schema is changed the version number has to be increased by one and a migration strategy needs to be
 * provided, otherwise the database will be deleted and recreated.
 *
 * More info:
 * https://developer.android.com/topic/libraries/architecture/room
 */
@Database(entities = [Identifier::class, HolderIdentifierData::class], version = 3)
internal abstract class SdkDatabase : RoomDatabase() {

    abstract fun identifierDao(): IdentifierDao

    abstract fun holderIdentifierDataDao(): HolderIdentifierDataDao
}

object SdkDbMigrations {
    val MIGRATIONS: Array<Migration> = arrayOf(
        object : Migration(2, 3) {
            @Suppress("MaxLineLength")
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `HolderIdentifierData` (`keyId` TEXT NOT NULL, `id` TEXT NOT NULL, `didMethod` TEXT NOT NULL, `algorithm` TEXT NOT NULL, `keyReference` TEXT NOT NULL, PRIMARY KEY(`keyId`))")
            }
        }
    )
}