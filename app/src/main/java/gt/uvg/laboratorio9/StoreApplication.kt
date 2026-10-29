
package gt.uvg.laboratorio9

import android.app.Application
import com.google.firebase.FirebaseApp

class StoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}
