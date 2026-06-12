package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда удаления элементов.
 * Удаляет все элементы, превышающие заданный.
 */
class RemoveGreaterCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "remove_greater"
    override val description = "удалить из коллекции элементы больше заданного"

    override fun execute(request: Request): Response {

        val args = request.argument?.split(" ") ?: emptyList()

        if (args.size < 2) {
            return Response("Укажите параметр и значение")
        }

        val param = args[0]
        val value = args[1].toDoubleOrNull()
            ?: return Response("Значение должно быть числом")

        val result = collectionManager.removeGreater(param, value, request.login)
        return Response(result)
    }
}