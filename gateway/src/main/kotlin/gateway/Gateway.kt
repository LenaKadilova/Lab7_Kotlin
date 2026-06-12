package gateway

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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

class Gateway(private val gatewayPort: Int, private val registrationPort: Int) {

    private val logger = LoggerFactory.getLogger(Gateway::class.java)
    private val servers = CopyOnWriteArrayList<InetSocketAddress>()
    private var roundRobinIndex = 0
    private val forkJoinPool = ForkJoinPool.commonPool()
    private val fixedThreadPool = Executors.newFixedThreadPool(4)
    private val serverChannel = DatagramChannel.open().apply {
        configureBlocking(false)
        bind(InetSocketAddress(12347))
    }
    private val knownClients = mutableSetOf<String>()

    fun start() {
        Thread { listenForRegistrations() }.start()
        listenForClients()
    }

    private fun listenForRegistrations() {
        val channel = DatagramChannel.open()
        channel.bind(InetSocketAddress(registrationPort))
        logger.info("Gateway ожидает регистрации серверов на порту $registrationPort")

        val buffer = ByteBuffer.allocate(1024)
        while (true) {
            buffer.clear()
            val address = channel.receive(buffer) as? InetSocketAddress ?: continue
            buffer.flip()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val message = String(bytes).trim()

            when {
                message.startsWith("REGISTER:") -> {
                    val port = message.removePrefix("REGISTER:").trim().toIntOrNull() ?: continue
                    val serverAddress = InetSocketAddress(address.address, port)
                    if (!servers.contains(serverAddress)) {
                        servers.add(serverAddress)
                        logger.info("Зарегистрирован сервер: $serverAddress. Всего серверов: ${servers.size}")
                    }
                    channel.send(ByteBuffer.wrap("OK".toByteArray()), address)
                }
                message.startsWith("UNREGISTER:") -> {
                    val port = message.removePrefix("UNREGISTER:").trim().toIntOrNull() ?: continue
                    val serverAddress = InetSocketAddress(address.address, port)
                    servers.remove(serverAddress)
                    logger.info("Сервер отключился: $serverAddress. Всего серверов: ${servers.size}")
                }
            }
        }
    }

    private fun listenForClients() {
        val channel = DatagramChannel.open()
        channel.bind(InetSocketAddress(gatewayPort))
        logger.info("Gateway запущен на порту $gatewayPort")

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
                    val response = forwardToServer(request)

                    Thread {
                        val baos = ByteArrayOutputStream()
                        ObjectOutputStream(baos).use { it.writeObject(response) }
                        synchronized(channel) {
                            channel.send(ByteBuffer.wrap(baos.toByteArray()), clientAddress)
                        }
                    }.start()
                }
            }
        }
    }

    private fun forwardToServer(request: Request): Response {
        if (servers.isEmpty()) {
            return Response("Нет доступных серверов")
        }

        val maxAttempts = servers.size
        repeat(maxAttempts) {
            val server = nextServer() ?: return Response("Нет доступных серверов")
            try {
                val response = sendToServer(request, server)
                if (response != null) return response
                logger.warn("Сервер $server не ответил, исключаем")
                servers.remove(server)
            } catch (e: Exception) {
                logger.warn("Ошибка при обращении к серверу $server: ${e.message}")
                servers.remove(server)
            }
        }
        return Response("Все серверы недоступны")
    }

    @Synchronized
    private fun nextServer(): InetSocketAddress? {
        if (servers.isEmpty()) return null
        val server = servers[roundRobinIndex % servers.size]
        roundRobinIndex++
        return server
    }

    private fun sendToServer(request: Request, server: InetSocketAddress): Response? {
        return try {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(request) }
            serverChannel.send(ByteBuffer.wrap(baos.toByteArray()), server)

            val receiveBuffer = ByteBuffer.allocate(65535)
            val deadline = System.currentTimeMillis() + 3000
            while (System.currentTimeMillis() < deadline) {
                receiveBuffer.clear()
                val addr = serverChannel.receive(receiveBuffer)
                if (addr != null) {
                    receiveBuffer.flip()
                    val bytes = ByteArray(receiveBuffer.remaining())
                    receiveBuffer.get(bytes)
                    return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Response }
                }
                Thread.sleep(10)
            }
            null
        } catch (e: Exception) {
            logger.warn("Ошибка при обращении к серверу $server: ${e.message}")
            null
        }
    }
}