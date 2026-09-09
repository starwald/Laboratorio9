package gt.uvg.laboratorio9.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ProfileKey(
    val producerId: Int
) : NavKey