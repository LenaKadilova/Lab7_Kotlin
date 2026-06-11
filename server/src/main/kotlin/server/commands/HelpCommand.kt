package server.commands

import server.CommandManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда вывода справки.
 * Показывает список всех доступных команд и их описание.
 */
class HelpCommand(private val commandManager: CommandManager) : Command {
    override val name = "help"
    override val description = "вывести справку по доступным командам"
    override val loggable = false

    override fun execute(request: Request): Response {
        val text = buildString {
            appendLine("Доступные команды:")
            for (command in commandManager.allCommands()) {
                appendLine("${command.name}: ${command.description}")
            }
        }
        return Response(text)
    }
}
