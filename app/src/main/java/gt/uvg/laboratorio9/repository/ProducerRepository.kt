package gt.uvg.laboratorio9.repository

import gt.uvg.laboratorio9.model.Producer
import kotlinx.coroutines.flow.Flow

interface ProducerRepository {
    fun observeProducers(): Flow<List<Producer>>
}
