package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда фильтрации элементов.
 * Выводит элементы, имя которых начинается с заданной строки.
 */
class FilterStartsWithNameCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "filter_starts_with_name"
    override val description = "вывести элементы, имя которых начинается с подстроки"

    override fun execute(request: Request): Response {
        val args = request.argument?.split(" ") ?: emptyList()

        if (args.isEmpty()) {
            return Response("Введите строку")
        }

        val prefix = args[0]
        return Response("Результат:", lines = collectionManager.filterStartsWithName(prefix))
    }
}