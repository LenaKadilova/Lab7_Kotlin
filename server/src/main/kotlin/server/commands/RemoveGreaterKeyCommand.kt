package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

class RemoveGreaterKeyCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "remove_greater_key"
    override val description = "удалить из коллекции все элементы, ключ которых больше заданного"

    override fun execute(request: Request): Response {
        val args = request.argument?.split(" ") ?: emptyList()

        if (args.isEmpty()) {
            return Response("Необходимо указать ключ")
        }

        val key = try {
            args[0].toLong()
        } catch (e: NumberFormatException) {
            return Response("Ключ должен быть числом")
        }

        collectionManager.removeGreaterKey(key)
        return Response("Элементы удалены")
    }
}