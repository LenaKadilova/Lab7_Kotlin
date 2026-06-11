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
            ?: return Response("Необходимо указать числовой ключ")

        if (collectionManager.storage.containsKey(key)) {
            return Response("Уже существует элемент с таким ключом")
        }

        val dragon = request.dragon
            ?: return Response("Дракон не передан в запросе")

        val id = collectionManager.nextId()
        collectionManager.storage[key] = dragon.copy(id = id)
        return Response("Дракон добавлен")
    }
}