package server.commands

import server.CollectionManager
import common.exceptions.ValidationException
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда обновления элемента.
 * Обновляет значение элемента по заданному id.
 */
class UpdateCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "update"
    override val description = "обновить элемент по id"
    override val requiresDragon = true

    override fun execute(request: Request): Response {
        val args = request.argument?.split(" ") ?: emptyList()

        if (args.isEmpty()) {
            return Response("Необходимо указать id")
        }

        val id = try {
            args[0].toLong()
        } catch (e: NumberFormatException) {
            return Response("id должен быть числом")
        }

        val dragon = request.dragon
            ?: return Response("Дракон не передан в запросе")

        return try {
            val result = collectionManager.updateById(id, dragon, request.login)
            Response(result)
        } catch (e: ValidationException) {
            Response("Ошибка: ${e.message}")
        }
    }
}