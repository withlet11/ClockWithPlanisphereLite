package io.github.withlet11.clockwithplanispherelite.model

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
abstract class CwpDataBase : RoomDatabase() {
    abstract fun cwpDao(): CwpDao

    companion object {
        @Volatile
        private var INSTANCE: CwpDataBase? = null

        fun getInstance(context: Context): CwpDataBase {
            println("Getting database")
            return INSTANCE ?: synchronized(this) {
                println("Creating database")
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CwpDataBase::class.java,
                    "clockwithplanisphere.db"
                ).createFromAsset("clockwithplanisphere.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build().also {
                        INSTANCE = it
                    }
            }

        }
    }
}
