package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TradePositionEntity::class,
        CompanyRecordEntity::class,
        StrategyConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KrunalQuantDatabase : RoomDatabase() {
    abstract fun quantDao(): KrunalQuantDao

    companion object {
        @Volatile
        private var INSTANCE: KrunalQuantDatabase? = null

        fun getInstance(context: Context): KrunalQuantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KrunalQuantDatabase::class.java,
                    "krunal_capital_quant_vault.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
