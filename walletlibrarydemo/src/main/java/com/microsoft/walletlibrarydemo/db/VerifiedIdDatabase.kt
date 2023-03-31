/*
package com.microsoft.walletlibrarydemo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.microsoft.walletlibrarydemo.db.entities.VerifiedId

@Database(entities = [VerifiedId::class], version = 1)
@TypeConverters(WalletLibraryTypeConverter::class)
abstract class VerifiedIdDatabase : RoomDatabase() {

    companion object {
        private var instance: VerifiedIdDatabase? = null

        @Synchronized
        fun getInstance(context: Context): VerifiedIdDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    context, VerifiedIdDatabase::class.java,
                    "wallet-library-db"
                ).fallbackToDestructiveMigration().build()
            return instance as VerifiedIdDatabase
        }
    }

    abstract fun verifiedIdDao(): VerifiedIdDao
}*/
