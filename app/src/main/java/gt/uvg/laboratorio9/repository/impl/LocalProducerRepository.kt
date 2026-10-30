package gt.uvg.laboratorio9.repository.impl

import gt.uvg.laboratorio9.data.producers
import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.repository.ProducerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LocalProducerRepository : ProducerRepository {

    override fun observeProducers(): Flow<List<Producer>> =
        flowOf(producers)
}