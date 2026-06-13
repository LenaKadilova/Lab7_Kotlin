package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command

/**
 * Команда вывода информации о коллекции.
 * Отображает тип коллекции, дату инициализации и количество элементов.
 */
class InfoCommand(private val collectionManager: CollectionManager) : Command {
    override val name = "info"
    override val description = "вывести информацию о коллекции (тип, дата инициализации, количество элементов, файл)"

    override fun execute(request: Request): Response {
        val size = collectionManager.loadFromDatabaseFresh().size
        val text = """
        Тип коллекции: java.util.Hashtable
        Дата инициализации: ${collectionManager.time}
        Количество элементов: $size
    """.trimIndent()
        return Response(text)
    }
}