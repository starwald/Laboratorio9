package gt.uvg.laboratorio9.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteIds(): Flow<Set<Int>>
    suspend fun addFavorite(productId: Int): Result<Unit>
    suspend fun removeFavorite(productId: Int): Result<Unit>
}

