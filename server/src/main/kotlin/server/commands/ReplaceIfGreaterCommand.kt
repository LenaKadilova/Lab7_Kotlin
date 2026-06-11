package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда замены элемента.
 * Заменяет значение по ключу, если новое значение больше текущего.
 */
class ReplaceIfGreaterCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "replace_if_greater"
    override val description = "заменить значение по ключу, если новое значение больше старого"

    override fun execute(request: Request): Response {

        val args = request.argument?.split(" ") ?: emptyList()

        if (args.size < 3) {
            return Response("Укажите ключ, параметр и значение")
        }

        val key = args[0].toLongOrNull()
            ?: return Response("Ключ должен быть числом")

        val param = args[1]

        val value = args[2].toDoubleOrNull()
            ?: return Response("Значение должно быть числом")

        val result = collectionManager.replaceIfGreater(key, param, value)

        return Response(result)
    }
}