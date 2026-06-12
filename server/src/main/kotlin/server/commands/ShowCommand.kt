package server.commands


import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда вывода элементов коллекции.
 * Отображает все элементы в строковом представлении.
 */
class ShowCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "show"
    override val description = "вывести все элементы коллекции"

    override fun execute(request: Request): Response {
        return Response("Коллекция:", lines = collectionManager.showAll())
    }
}