package gt.uvg.laboratorio9.repository

interface AuthRepository {
    suspend fun ensureAnonymousSession(): Result<String>
}

