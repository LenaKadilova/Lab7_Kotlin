package server

import server.commands.*
import common.commands.ExitCommand
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    val logger = LoggerFactory.getLogger("server.Main")

    val db = DatabaseManager()

    try {
        db.initTables()
        logger.info("Таблицы инициализированы")
    } catch (e: Exception) {
        logger.error("Ошибка инициализации БД: ${e.message}")
        return
    }

    val collectionManager = CollectionManager(LocalDateTime.now(), db)

    try {
        collectionManager.loadFromDatabase()
        logger.info("Коллекция загружена: ${collectionManager.size()} элементов")
    } catch (e: Exception) {
        logger.error("Не удалось загрузить коллекцию: ${e.message}")
    }

    val commandManager = CommandManager()

    commandManager.addToList(HelpCommand(commandManager))
    commandManager.addToList(SaveCommand(collectionManager))
    commandManager.addToList(InfoCommand(collectionManager))
    commandManager.addToList(ShowCommand(collectionManager))
    commandManager.addToList(ClearCommand(collectionManager))
    commandManager.addToList(PrintAscendingCommand(collectionManager))
    commandManager.addToList(InsertCommand(collectionManager))
    commandManager.addToList(UpdateCommand(collectionManager))
    commandManager.addToList(RemoveGreaterKeyCommand(collectionManager))
    commandManager.addToList(FilterStartsWithNameCommand(collectionManager))
    commandManager.addToList(GroupCountingByIdCommand(collectionManager))
    commandManager.addToList(RemoveGreaterCommand(collectionManager))
    commandManager.addToList(RemoveKeyCommand(collectionManager))
    commandManager.addToList(ReplaceIfGreaterCommand(collectionManager))
    commandManager.addToList(ExitCommand())

    val port = args.firstOrNull()?.toIntOrNull() ?: 12345
    val server = Server(commandManager, db, port)
    server.start()
}