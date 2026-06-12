package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда добавления элемента.
 * Добавляет новый элемент с заданным ключом в коллекцию.
 */
class InsertCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "insert"
    override val description = "добавить новый элемент с заданным ключом"
    override val requiresDragon = true

    override fun execute(request: Request): Response {
        val key = request.argument?.toLongOrNull()
            ?: return Response("Ошибка: необходимо указать числовой ключ")

        if (collectionManager.storage.containsKey(key)) {
            return Response("Ошибка: уже существует элемент с таким ключом")
        }

        if (request.dragon == null) {
            return Response("OK", needsDragon = true)
        }

        val result = collectionManager.insert(request.dragon!!, request.login)
        return Response(result)
    }
}