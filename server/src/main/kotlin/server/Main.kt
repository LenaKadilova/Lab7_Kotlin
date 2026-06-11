package server

import common.commands.*
import server.commands.HelpCommand
import server.commands.SaveCommand
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import server.commands.ClearCommand
import server.commands.FilterStartsWithNameCommand
import server.commands.GroupCountingByIdCommand
import server.commands.InfoCommand
import server.commands.InsertCommand
import server.commands.PrintAscendingCommand
import server.commands.RemoveGreaterCommand
import server.commands.RemoveGreaterKeyCommand
import server.commands.RemoveKeyCommand
import server.commands.ReplaceIfGreaterCommand
import server.commands.ShowCommand
import server.commands.UpdateCommand

fun main(args: Array<String>) {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    val logger = LoggerFactory.getLogger("server.Main")

    if (args.isEmpty()) {
        logger.error("Ошибка: нужно передать имя файла")
        return
    }

    val fileName = args[0]
    val fileManager = FileManager(fileName)
    val collectionManager = CollectionManager(LocalDateTime.now(), fileName)
    val commandManager = CommandManager()

    try {
        collectionManager.loadCollectionFromFile(fileManager)
        logger.info("Коллекция загружена: ${collectionManager.size()} элементов")
    } catch (e: Exception) {
        logger.error("Не удалось загрузить коллекцию: ${e.message}")
    }

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

    val server = Server(collectionManager, commandManager)
    server.start()
}