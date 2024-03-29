package com.microsoft.walletlibrarydemo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.microsoft.walletlibrarydemo.db.entities.EncodedVerifiedId

@Database(entities = [EncodedVerifiedId::class], version = 1)
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
}
