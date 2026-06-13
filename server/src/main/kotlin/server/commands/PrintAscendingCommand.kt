package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда сортировки элементов.
 * Выводит элементы коллекции в порядке возрастания.
 */
class PrintAscendingCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "print_ascending"
    override val description = "вывести элементы в порядке возрастания"

    override fun execute(request: Request): Response {
        val param = request.argument ?: return Response("Укажите параметр сортировки")
        val dragons = collectionManager.loadFromDatabaseFresh().values
        if (dragons.isEmpty()) return Response("Элементы не найдены")
        val sorted = when (param) {
            "id" -> dragons.sortedBy { it.id }
            "x" -> dragons.sortedBy { it.coordinates.x }
            "y" -> dragons.sortedBy { it.coordinates.y }
            "creationDate" -> dragons.sortedBy { it.creationDate }
            "age" -> dragons.sortedBy { it.age }
            "weight" -> dragons.sortedBy { it.weight }
            "eyesCount" -> dragons.sortedBy { it.head?.eyesCount }
            "toothCount" -> dragons.sortedBy { it.head?.toothCount }
            else -> return Response("Неверный параметр")
        }
        return Response("Отсортированная коллекция:", lines = sorted.map { it.toString() })
    }
}