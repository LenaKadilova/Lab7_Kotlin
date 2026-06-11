package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда удаления элемента по ключу.
 * Удаляет элемент коллекции по заданному ключу.
 */
class RemoveKeyCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "remove_key"
    override val description = "удалить элемент по ключу"

    override fun execute(request: Request): Response {
        val args = request.argument?.split(" ") ?: emptyList()

        if (args.isEmpty()) {
            return Response("Введите ключ")
        }

        val key = try {
            args[0].toLong()
        } catch (e: NumberFormatException) {
            return Response("Ключ должен быть числом")
        }

        collectionManager.removeByKey(key)
        return Response("Элемент удалён")
    }
}