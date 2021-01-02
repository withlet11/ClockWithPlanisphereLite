package io.github.withlet11.skyclocklite.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AbstractSkyModel.HipEntry::class,
        AbstractSkyModel.ConstellationLineEntry::class,
        AbstractSkyModel.NorthMilkyWayDotEntry::class,
        AbstractSkyModel.SouthMilkyWayDotEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SkyClockDataBase : RoomDatabase() {
    abstract fun skyClockDao(): SkyClockDao

    companion object {
        @Volatile
        private var INSTANCE: SkyClockDataBase? = null

        fun getInstance(context: Context): SkyClockDataBase {
            println("Getting database")
            return INSTANCE ?: synchronized(this) {
                println("Creating database")
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkyClockDataBase::class.java,
                    "skyclock.db"
                ).createFromAsset("skyclock.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build().also {
                        INSTANCE = it
                    }
            }

        }
    }
}
