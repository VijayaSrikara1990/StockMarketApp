package com.example.stockmarketapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [CompanyListingEntity::class]
)
abstract class StockDatabase: RoomDatabase() {
    abstract val dao: StockDao
}