
package gt.uvg.laboratorio9.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import gt.uvg.laboratorio9.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreProductDataSource(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    fun observeProducts(): Flow<Result<List<Product>>> =
        callbackFlow {

            val registration =
                firestore.collection("products")
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            trySend(Result.failure(error))
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            return@addSnapshotListener
                        }

                        val result = runCatching {

                            snapshot.documents.map { document ->

                                Product(
                                    id = requireNotNull(
                                        document.getLong("id")
                                    ).toInt(),

                                    name = requireNotNull(
                                        document.getString("name")
                                    ),

                                    description = requireNotNull(
                                        document.getString("description")
                                    ),

                                    price = requireNotNull(
                                        document.getDouble("price")
                                    ),

                                    producerId = requireNotNull(
                                        document.getLong("producerId")
                                    ).toInt(),

                                    stock = requireNotNull(
                                        document.getLong("stock")
                                    ).toInt(),

                                    imageUrl = requireNotNull(
                                        document.getString("imageUrl")
                                    )
                                )
                            }.sortedBy { it.id }
                        }

                        trySend(result)
                    }

            awaitClose {
                registration.remove()
            }
        }
}
