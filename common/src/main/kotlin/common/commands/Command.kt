package common.commands

import common.Request
import common.Response

/**
 * Интерфейс для команд.
 */
interface Command {
    val name: String
    val description: String
    val requiresDragon: Boolean get() = false
    val loggable: Boolean get() = true
    fun execute(request: Request): Response
}
