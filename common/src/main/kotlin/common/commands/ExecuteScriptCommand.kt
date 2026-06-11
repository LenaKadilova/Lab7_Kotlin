package common.commands

import common.Request
import common.Response

/**
 * Команда выполнения скрипта.
 * Считывает команды из файла и выполняет их последовательно.
 */
class ExecuteScriptCommand : Command {

    override val name = "execute_script"
    override val description = "выполнить скрипт из файла"

    override fun execute(request: Request): Response {
        return Response("Скрипт обрабатывается на клиенте")
    }
}