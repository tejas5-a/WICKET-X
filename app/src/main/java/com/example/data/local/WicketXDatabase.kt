package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MatchEntity::class,
        DeliveryEntity::class,
        DeliveryAuditEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        TournamentEntity::class,
        UserAccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WicketXDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun deliveryAuditDao(): DeliveryAuditDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: WicketXDatabase? = null

        fun getDatabase(context: Context): WicketXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WicketXDatabase::class.java,
                    "wicketx_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
