package server

import common.Request
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class Server(private val commandManager: CommandManager) {

    private val logger = LoggerFactory.getLogger(Server::class.java)
    private val knownClients = mutableSetOf<String>()

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

            val request = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Request }

            val loggable = commandManager.isLoggable(request.commandName)
            if (loggable) logger.info("Получен запрос: ${request.commandName}")

            val response = commandManager.execute(request).copy(
                commands = commandManager.getCommandDescriptions(),
                commandsRequiringDragon = commandManager.getCommandsRequiringDragon()
            )

            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(response) }
            val responseData = baos.toByteArray()

            channel.send(ByteBuffer.wrap(responseData), clientAddress)

            if (loggable) logger.info("Отправлен ответ на команду: ${request.commandName}")
        }
    }
}