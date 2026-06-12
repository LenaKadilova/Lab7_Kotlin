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

class Server(private val commandManager: CommandManager, private val db: DatabaseManager, private val port: Int = 12345) {

    private val logger = LoggerFactory.getLogger(Server::class.java)
    private val forkJoinPool = ForkJoinPool.commonPool()
    private val fixedThreadPool = Executors.newFixedThreadPool(4)
    private val gatewayAddress = InetSocketAddress("127.0.0.1", 12346)
    private val registrationChannel = DatagramChannel.open().apply { configureBlocking(false) }

    fun start() {
        val channel = DatagramChannel.open()
        channel.bind(InetSocketAddress(port))
        logger.info("Сервер запущен на порту $port")

        registerAtGateway()

        Runtime.getRuntime().addShutdownHook(Thread {
            unregisterFromGateway()
            logger.info("Сервер остановлен")
        })

        val buffer = ByteBuffer.allocate(65535)

        while (true) {
            buffer.clear()
            val clientAddress = channel.receive(buffer) as? InetSocketAddress ?: continue

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

        if (request.commandName == "login") {
            return handleLogin(request)
        }

        val login = if (request.token != null) {
            db.validateToken(request.token!!) ?: return Response("Токен недействителен, войдите снова")
        } else {
            return Response("Необходима авторизация")
        }

        val requestWithLogin = request.copy(login = login)

        val loggable = commandManager.isLoggable(request.commandName)
        if (loggable) logger.info("Получен запрос: ${request.commandName} от $login")

        if (request.commandName == "exit") {
            request.token.let { db.invalidateToken(it) }
            return Response("До свидания!", exitClient = true)
        }

        val response = commandManager.execute(requestWithLogin).copy(
            commands = commandManager.getCommandDescriptions(),
            commandsRequiringDragon = commandManager.getCommandsRequiringDragon()
        )

        if (loggable) logger.info("Отправлен ответ на команду: ${request.commandName}")
        return response
    }

    private fun handleLogin(request: Request): Response {
        if (request.login.isBlank() || request.passwordHash.isBlank()) {
            return Response("Логин и пароль не могут быть пустыми")
        }
        if (!db.authenticateUser(request.login, request.passwordHash)) {
            return Response("Ошибка авторизации: неверный логин или пароль")
        }
        val token = db.createToken(request.login)
        return Response("Вход выполнен", token = token)
    }

    private fun handleRegister(request: Request): Response {
        if (request.login.isBlank() || request.passwordHash.isBlank()) {
            return Response("Логин и пароль не могут быть пустыми")
        }
        val success = db.registerUser(request.login, request.passwordHash)
        return if (success) Response("Регистрация успешна") else Response("Пользователь с таким логином уже существует")
    }

    private fun registerAtGateway() {
        try {
            val message = "REGISTER:$port".toByteArray()
            registrationChannel.send(ByteBuffer.wrap(message), gatewayAddress)
            logger.info("Отправлена регистрация у gateway")
        } catch (e: Exception) {
            logger.warn("Не удалось зарегистрироваться у gateway: ${e.message}")
        }
    }

    private fun unregisterFromGateway() {
        try {
            val message = "UNREGISTER:$port".toByteArray()
            registrationChannel.send(ByteBuffer.wrap(message), gatewayAddress)
            logger.info("Отправлена отмена регистрации у gateway")
        } catch (e: Exception) {
            logger.warn("Не удалось отменить регистрацию у gateway: ${e.message}")
        }
    }
}