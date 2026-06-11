package server.commands

import server.CollectionManager
import common.Request
import common.Response
import common.commands.Command
/**
 * Команда сохранения коллекции.
 * Сохраняет текущее состояние коллекции в файл.
 */
class SaveCommand(private val collectionManager: CollectionManager) : Command {

    override val name = "save"
    override val description = "сохранить коллекцию в файл"

    override fun execute(request: Request): Response {
        val args = request.argument?.split(" ") ?: emptyList()

        if (args.isEmpty()) {
            return Response("Ошибка: нужно указать имя файла")
        }

        val fileName = args[0]
        collectionManager.save(fileName)
        return Response("Коллекция сохранена")
    }
}