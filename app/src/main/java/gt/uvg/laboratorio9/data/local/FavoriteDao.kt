package gt.uvg.laboratorio9.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(
        favorite: FavoriteEntity
    )

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavorite(
        productId: Int
    )
}