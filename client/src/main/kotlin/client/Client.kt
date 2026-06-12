package client

import common.Request
import common.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class Client(private val host: String, private val port: Int, private val io: IOManager) {

    private val channel = DatagramChannel.open().apply {
        configureBlocking(false)
    }
    private val serverAddress = InetSocketAddress(host, port)

    fun send(request: Request): Response? {
        return try {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(request) }
            val data = baos.toByteArray()
            val sendBuffer = ByteBuffer.wrap(data)
            channel.send(sendBuffer, serverAddress)

            val receiveBuffer = ByteBuffer.allocate(65535)
            val deadline = System.currentTimeMillis() + 5000
            while (System.currentTimeMillis() < deadline) {
                receiveBuffer.clear()
                val addr = channel.receive(receiveBuffer)
                if (addr != null) {
                    receiveBuffer.flip()
                    val bytes = ByteArray(receiveBuffer.remaining())
                    receiveBuffer.get(bytes)
                    return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Response }
                }
                Thread.sleep(10)
            }
            io.println("Сервер недоступен: превышено время ожидания")
            null
        } catch (e: Exception) {
            io.println("Сервер недоступен: ${e.message}")
            null
        }
    }

    fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun close() {
        channel.close()
    }
}