package common.commands

import common.Request
import common.Response

/**
 * Команда выхода из программы.
 * Завершает выполнение без сохранения коллекции.
 */
class ExitCommand : Command {
    override val name = "exit"
    override val description = "завершить программу"

    override fun execute(request: Request): Response {
        return Response("Клиент завершён", exitClient = true)
    }
}