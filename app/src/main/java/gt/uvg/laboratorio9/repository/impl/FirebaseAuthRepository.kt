package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.firebase.FirebaseAuthManager
import gt.uvg.laboratorio9.repository.AuthRepository
import kotlinx.coroutines.CancellationException

class FirebaseAuthRepository(
    private val authManager: FirebaseAuthManager
) : AuthRepository {

    override suspend fun ensureAnonymousSession(): Result<String> {
        return try {
            Result.success(authManager.ensureAnonymousSession())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}