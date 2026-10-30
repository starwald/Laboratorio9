package gt.uvg.laboratorio9.repository

interface StockRepository {
    suspend fun reserveUnit(productID: Int): Result<Unit>
    suspend fun releaseUnit(productID: Int): Result<Unit>
}