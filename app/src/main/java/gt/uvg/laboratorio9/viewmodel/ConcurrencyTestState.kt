package gt.uvg.laboratorio9.viewmodel

sealed interface ConcurrencyTestState {
    data object Idle : ConcurrencyTestState
    data object Running : ConcurrencyTestState

    data class Completed (
        val attempts: Int,
        val successes: Int,
        val rejections: Int
    ) : ConcurrencyTestState

    data class Error (
        val message: String
    ) : ConcurrencyTestState
}