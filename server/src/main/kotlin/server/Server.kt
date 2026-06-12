package server

import common.Request
import common.Response
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

class Server(private val commandManager: CommandManager, private val db: DatabaseManager) {

    private val logger = LoggerFactory.getLogger(Server::class.java)
    private val knownClients = mutableSetOf<String>()
    private val forkJoinPool = ForkJoinPool.commonPool()
    private val fixedThreadPool = Executors.newFixedThreadPool(4)

    fun start() {
        val channel = DatagramChannel.open()
        channel.bind(InetSocketAddress(12345))
        logger.info("Сервер запущен на порту 12345")

        val buffer = ByteBuffer.allocate(65535)

        while (true) {
            buffer.clear()
            val clientAddress = channel.receive(buffer) as? InetSocketAddress ?: continue

            val clientKey = clientAddress.toString()
            if (knownClients.add(clientKey)) {
                logger.info("Новое подключение: $clientAddress")
            }

            buffer.flip()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            forkJoinPool.submit {
                val request = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Request }

                fixedThreadPool.submit {
                    val response = processRequest(request)

                    Thread {
                        val baos = ByteArrayOutputStream()
                        ObjectOutputStream(baos).use { it.writeObject(response) }
                        val responseData = baos.toByteArray()
                        synchronized(channel) {
                            channel.send(ByteBuffer.wrap(responseData), clientAddress)
                        }
                    }.start()
                }
            }
        }
    }

    private fun processRequest(request: Request): Response {
        if (request.commandName == "register") {
            return handleRegister(request)
        }

        if (!db.authenticateUser(request.login, request.passwordHash)) {
            return Response("Ошибка авторизации: неверный логин или пароль")
        }

        val loggable = commandManager.isLoggable(request.commandName)
        if (loggable) logger.info("Получен запрос: ${request.commandName} от ${request.login}")

        val response = commandManager.execute(request).copy(
            commands = commandManager.getCommandDescriptions(),
            commandsRequiringDragon = commandManager.getCommandsRequiringDragon()
        )

        if (loggable) logger.info("Отправлен ответ на команду: ${request.commandName}")
        return response
    }

    private fun handleRegister(request: Request): Response {
        val login = request.login
        val passwordHash = request.passwordHash
        if (login.isBlank() || passwordHash.isBlank()) {
            return Response("Логин и пароль не могут быть пустыми")
        }
        val success = db.registerUser(login, passwordHash)
        return if (success) Response("Регистрация успешна") else Response("Пользователь с таким логином уже существует")
    }
}