package client

import common.Request
import common.Response
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.net.ConnectException

class Client(private val host: String, private val port: Int, private val io: IOManager) {

    fun send(request: Request): Response? {
        return try {
            val socket = Socket(host, port)
            val out = ObjectOutputStream(socket.getOutputStream())
            out.flush()
            val input = ObjectInputStream(socket.getInputStream())
            out.writeObject(request)
            val response = input.readObject() as Response
            socket.close()
            response
        } catch (e: ConnectException) {
            io.println("Сервер недоступен: ${e.message}")
            null
        }
    }
}