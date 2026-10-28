package gt.uvg.laboratorio9.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        OrderLineEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    abstract fun orderLineDao(): OrderLineDao
}