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
        return Response("Группировка:", lines = collectionManager.groupCountingById())
    }
}