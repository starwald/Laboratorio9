package gt.uvg.laboratorio9.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

object StoreDatabaseProvider {

    @Volatile
    private var INSTANCE: StoreDatabase? = null

    fun getDatabase(context: Context): StoreDatabase {

        return INSTANCE ?: synchronized(this) {

            INSTANCE ?: Room.databaseBuilder<StoreDatabase>(
                context.applicationContext,
                "store_database"
            )
                .setDriver(AndroidSQLiteDriver())
                .build()
                .also { database ->
                    INSTANCE = database
                }
        }
    }
}