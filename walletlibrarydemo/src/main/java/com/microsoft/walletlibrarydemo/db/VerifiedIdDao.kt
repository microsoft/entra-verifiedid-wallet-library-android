package com.microsoft.walletlibrarydemo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.walletlibrarydemo.db.entities.EncodedVerifiedId

@Dao
interface VerifiedIdDao {

    @Insert
    suspend fun insert(encodedVerifiedId: EncodedVerifiedId)

    @Delete
    suspend fun delete(encodedVerifiedId: EncodedVerifiedId)

    @Query("SELECT * FROM EncodedVerifiedId")
    suspend fun queryVerifiedIds(): List<EncodedVerifiedId>

    @Query("SELECT * FROM EncodedVerifiedId WHERE vcId = :id")
    suspend fun queryVerifiedIdById(id: String): EncodedVerifiedId
}
