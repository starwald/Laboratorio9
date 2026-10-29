
package gt.uvg.laboratorio9.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(
    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
) {

    suspend fun ensureAnonymousSession(): String {

        auth.currentUser?.let { user ->
            return user.uid
        }

        val result = auth.signInAnonymously().await()

        return requireNotNull(result.user?.uid) {
            "No se pudo iniciar sesión anónima"
        }
    }
}
