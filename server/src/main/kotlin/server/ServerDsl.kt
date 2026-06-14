package server

import server.commands.*
import common.commands.ExitCommand
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

class ServerDsl {
    private val logger = LoggerFactory.getLogger(ServerDsl::class.java)
    lateinit var db: DatabaseManager
    lateinit var collectionManager: CollectionManager
    lateinit var commandManager: CommandManager
    private lateinit var server: Server

    fun initialize() {
        db = DatabaseManager()
        try {
            db.initTables()
            logger.info("Таблицы инициализированы")
        } catch (e: Exception) {
            logger.error("Ошибка инициализации БД: ${e.message}")
            throw e
        }

        collectionManager = CollectionManager(LocalDateTime.now(), db)
        try {
            collectionManager.loadFromDatabase()
            logger.info("Коллекция загружена: ${collectionManager.size()} элементов")
        } catch (e: Exception) {
            logger.error("Не удалось загрузить коллекцию: ${e.message}")
        }

        commandManager = CommandManager()
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
    }

    fun start(block: StartDsl.() -> Unit) {
        StartDsl(commandManager, db).apply(block).also {
            server = it.server!!
        }
    }

    fun startInteractiveMode() {
        server.start()
    }
}

class StartDsl(private val commandManager: CommandManager, private val db: DatabaseManager) {
    var server: Server? = null

    fun startServer(host: String, port: Int) {
        server = Server(commandManager, db, port)
    }

    fun sendRegistrationRequest(gatewayHost: String, gatewayPort: Int, message: String) {
        server?.registerAtGateway(gatewayHost, gatewayPort)
    }
}

fun server(block: ServerDsl.() -> Unit) {
    ServerDsl().apply(block)
}