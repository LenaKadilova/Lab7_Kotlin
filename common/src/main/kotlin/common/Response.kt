package common

import java.io.Serializable

data class Response(
    val message: String,
    val lines: List<String>? = null,
    val commands: Map<String, String> = emptyMap(),
    val commandsRequiringDragon: Set<String> = emptySet(),
    val exitClient: Boolean = false
) : Serializable