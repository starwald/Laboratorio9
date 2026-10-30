package gt.uvg.laboratorio9.repository

import com.google.firebase.firestore.FirebaseFirestore
import gt.uvg.laboratorio9.repository.StockRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreStockRepository(
    private val firestore: FirebaseFirestore
) : StockRepository {
    override suspend fun reserveUnit(productID: Int): Result<Unit> {
        return executeStockChange(
            productID = productID,
            change = -1
        )
    }

    override suspend fun releaseUnit(productID: Int): Result<Unit> {
        return executeStockChange(
            productID = productID,
            change = -1
        )
    }

    private suspend fun executeStockChange(
        productID: Int,
        change: Int
    ): Result<Unit> {
        return try {
            val reference = firestore
                .collection("products")
                .document(productID.toString())

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(reference)

                if (!snapshot.exists()) {
                    throw IllegalStateException("Producto no encontrado")
                }

                val currentStock = snapshot.getLong("stock")
                    ?: throw IllegalStateException("Existencias inválidas")

                if (currentStock < 0) {
                    throw IllegalStateException("Existencias inválidas")
                }

                if (change < 0 && currentStock == 0L) {
                    throw IllegalStateException("Producto agotado")
                }

                val newStock = currentStock + change

                if (newStock < 0 || newStock > Int.MAX_VALUE) {
                    throw IllegalStateException("No se puede modificar el stock")
                }

                transaction.update(reference, "stock", newStock)

                Unit
            }.await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}