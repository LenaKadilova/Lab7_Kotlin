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
        val dragons = collectionManager.loadFromDatabaseFresh()
        if (dragons.isEmpty()) return Response("Коллекция пустая")
        val lines = dragons.entries.flatMap { listOf("Ключ = ${it.key}", "Значение = ${it.value}") }
        return Response("Коллекция:", lines = lines)
    }
}