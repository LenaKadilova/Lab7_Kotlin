package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда группировки элементов.
 * Группирует элементы по id и выводит количество в каждой группе.
 */
class GroupCountingByIdCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "group_counting_by_id"
    override val description = "сгруппировать элементы по id"

    override fun execute(request: Request): Response {
        val grouped = collectionManager.loadFromDatabaseFresh().values.groupingBy { it.id }.eachCount()
        return Response("Группировка:", lines = if (grouped.isEmpty()) listOf("Коллекция пуста") else grouped.map { (id, count) -> "ID: $id -> количество: $count" })
    }
}