package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.firebase.FirestoreProductDataSource
import gt.uvg.laboratorio9.model.Product
import gt.uvg.laboratorio9.repository.AuthRepository
import gt.uvg.laboratorio9.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FirestoreProductRepository(
    private val dataSource: FirestoreProductDataSource,
    private val authRepository: AuthRepository
) : ProductRepository {

    override fun observeProducts(): Flow<Result<List<Product>>> = flow {
        try {
            authRepository.ensureAnonymousSession().getOrThrow()
            emitAll(dataSource.observeProducts())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}