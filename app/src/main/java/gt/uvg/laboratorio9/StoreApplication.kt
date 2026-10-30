package gt.uvg.laboratorio9

import android.app.Application
import com.google.firebase.FirebaseApp
import gt.uvg.laboratorio9.di.AppContainer

class StoreApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        container = AppContainer(this)
    }
}