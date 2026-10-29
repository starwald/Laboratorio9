package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.local.FavoriteDao
import gt.uvg.laboratorio9.data.local.FavoriteEntity
import gt.uvg.laboratorio9.repository.FavoriteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoriteRepository(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        favoriteDao.observeFavorites().map { favorites ->
            favorites.map { it.productId }.toSet()
        }

    override suspend fun addFavorite(productId: Int): Result<Unit> {
        return try {
            favoriteDao.insertFavorite(
                FavoriteEntity(productId = productId)
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(productId: Int): Result<Unit> {
        return try {
            favoriteDao.deleteFavorite(productId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}