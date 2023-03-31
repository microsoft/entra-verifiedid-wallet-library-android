package com.microsoft.walletlibrarydemo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.microsoft.walletlibrarydemo.db.entities.VerifiedId

@Dao
interface VerifiedIdDao {

    @Insert
    suspend fun insert(verifiedId: VerifiedId)

    @Delete
    suspend fun delete(verifiedId: VerifiedId)

    @Query("SELECT * FROM VerifiedId")
    suspend fun queryVerifiedIds(): List<VerifiedId>

    @Query("SELECT * FROM VerifiedId WHERE vcId = :id")
    suspend fun queryVerifiedIdById(id: String): VerifiedId
}
