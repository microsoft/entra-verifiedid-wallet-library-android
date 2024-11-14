// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.walletlibrary.datasource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microsoft.walletlibrary.datasource.db.entities.HolderIdentifierData

@Dao
internal interface HolderIdentifierDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(identifier: HolderIdentifierData)

    @Query("SELECT * FROM HolderIdentifierData")
    suspend fun queryAllHolderIdentifiers(): List<HolderIdentifierData>
}