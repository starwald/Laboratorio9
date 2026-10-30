package gt.uvg.laboratorio9.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import gt.uvg.laboratorio9.data.firebase.FirebaseAuthManager
import gt.uvg.laboratorio9.data.firebase.FirestoreProductDataSource
import gt.uvg.laboratorio9.data.local.CatalogPreferences
import gt.uvg.laboratorio9.data.local.StoreDatabaseProvider
import gt.uvg.laboratorio9.network.RetrofitProvider
import gt.uvg.laboratorio9.repository.AuthRepository
import gt.uvg.laboratorio9.repository.FavoriteRepository
import gt.uvg.laboratorio9.repository.OrderRepository
import gt.uvg.laboratorio9.repository.PreferencesRepository
import gt.uvg.laboratorio9.repository.ProductRepository
import gt.uvg.laboratorio9.repository.RemoteOrdersRepository
import gt.uvg.laboratorio9.repository.impl.DataStorePreferencesRepository
import gt.uvg.laboratorio9.repository.impl.FirebaseAuthRepository
import gt.uvg.laboratorio9.repository.impl.FirestoreProductRepository
import gt.uvg.laboratorio9.repository.impl.MockApiOrdersRepository
import gt.uvg.laboratorio9.repository.impl.RoomFavoriteRepository
import gt.uvg.laboratorio9.repository.impl.RoomOrderRepository
import gt.uvg.laboratorio9.repository.ProducerRepository
import gt.uvg.laboratorio9.repository.impl.LocalProducerRepository
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database by lazy {
        StoreDatabaseProvider.getDatabase(appContext)
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            authManager = FirebaseAuthManager(
                auth = FirebaseAuth.getInstance()
            )
        )
    }

    val productRepository: ProductRepository by lazy {
        FirestoreProductRepository(
            dataSource = FirestoreProductDataSource(firestore),
            authRepository = authRepository
        )
    }

    val favoriteRepository: FavoriteRepository by lazy {
        RoomFavoriteRepository(
            favoriteDao = database.favoriteDao()
        )
    }

    val orderRepository: OrderRepository by lazy {
        RoomOrderRepository(
            orderLineDao = database.orderLineDao()
        )
    }

    val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(
            catalogPreferences = CatalogPreferences(appContext)
        )
    }

    val remoteOrdersRepository: RemoteOrdersRepository by lazy {
        MockApiOrdersRepository(
            orderApi = RetrofitProvider.orderApi
        )
    }

    val producerRepository: ProducerRepository by lazy {
        LocalProducerRepository()
    }
}