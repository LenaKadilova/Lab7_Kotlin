package server

import common.commands.Command
import common.Request
import common.Response

/**
 * Класс для управления командами.
 * Хранит список доступных команд и выполняет их по имени.
 */
class CommandManager {
    private val commands: MutableMap<String, Command> = linkedMapOf()

    fun addToList(command: Command) {
        commands[command.name] = command
    }

    fun execute(request: Request): Response {
        val command = commands[request.commandName]
            ?: return Response("Команда не найдена")
        return command.execute(request)
    }

    fun allCommands(): List<Command> = commands.values.toList()

    fun getCommandDescriptions(): Map<String, String> =
        commands.mapValues { it.value.description }

    fun getCommandsRequiringDragon(): Set<String> =
        commands.values.filter { it.requiresDragon }.map { it.name }.toSet()

    fun isLoggable(commandName: String): Boolean =
        commands[commandName]?.loggable ?: true
}
