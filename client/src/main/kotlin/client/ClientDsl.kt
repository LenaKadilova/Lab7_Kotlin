package client

import common.Request

class ClientDsl {
    private lateinit var client: Client
    private lateinit var io: IOManager
    var authorized: Boolean = false
    private var token: String = ""
    private val commandsRequiringDragon = mutableSetOf<String>()
    private val executingScripts = mutableSetOf<String>()

    fun connect() {
        io = IOManager()
        client = Client("192.168.10.80", 12344, io)
    }

    fun authorize() {
        while (true) {
            io.println("Введите 'login' для входа или 'register' для регистрации:")
            io.print("> ")
            io.flush()
            when (io.readLine().trim()) {
                "login" -> {
                    io.println("Логин:")
                    val login = io.readLine().trim()
                    io.println("Пароль:")
                    val password = io.readLine().trim()
                    val passwordHash = client.hashPassword(password)

                    val response = client.send(Request("login", null, null, login, passwordHash))
                    if (response == null || response.token == null) {
                        io.println(response?.message ?: "Сервер недоступен")
                        continue
                    }
                    token = response.token.toString()
                    io.println(response.message)

                    val helpResponse = client.send(Request("help", null, null, token = token))
                    if (helpResponse != null) {
                        commandsRequiringDragon.addAll(helpResponse.commandsRequiringDragon)
                    }
                    authorized = true
                    return
                }
                "register" -> {
                    io.println("Логин:")
                    val login = io.readLine().trim()
                    io.println("Пароль:")
                    val password = io.readLine().trim()
                    val passwordHash = client.hashPassword(password)

                    val response = client.send(Request("register", null, null, login, passwordHash))
                    if (response == null) {
                        io.println("Сервер недоступен")
                        continue
                    }
                    io.println(response.message)
                    if (response.message == "Регистрация успешна") continue
                }
                else -> io.println("Неверная команда")
            }
        }
    }

    fun startInteractiveMode() {
        io.println("Клиент запущен. Введите команду:")

        while (true) {
            io.print("> ")
            io.flush()

            val line = io.readLine()
            if (io.currentFile == null) executingScripts.clear()
            if (line.isBlank()) continue

            val parts = line.trim().split(Regex("\\s+"))
            val commandName = parts[0]
            val argument = parts.drop(1).joinToString(" ").ifEmpty { null }

            if (commandName == "execute_script") {
                val fileName = argument ?: run { io.println("Укажите файл"); continue }
                if (executingScripts.contains(fileName)) {
                    io.println("Обнаружена рекурсия! Скрипт уже выполняется.")
                    continue
                }
                val file = java.io.File(fileName)
                if (!file.exists()) { io.println("Файл не найден: $fileName"); continue }
                executingScripts.add(fileName)
                io.setFileInput(file)
                continue
            }

            if (commandName in commandsRequiringDragon) {
                val checkResponse = client.send(Request(commandName, argument, null, token = token))
                if (checkResponse == null) { io.println("Сервер недоступен"); continue }
                if (checkResponse.message.startsWith("Ошибка") || checkResponse.message.startsWith("Нет прав")) {
                    io.println(checkResponse.message)
                    continue
                }
                if (!checkResponse.needsDragon) {
                    io.println(checkResponse.message)
                    checkResponse.lines?.forEach { io.println(it) }
                    continue
                }
                val dragon = io.createDragon(0)
                val response = client.send(Request(commandName, argument, dragon, token = token))
                if (response != null) {
                    commandsRequiringDragon.clear()
                    commandsRequiringDragon.addAll(response.commandsRequiringDragon)
                    io.println(response.message)
                    response.lines?.forEach { io.println(it) }
                    if (response.exitClient) { client.close(); return }
                }
                continue
            }

            val response = client.send(Request(commandName, argument, null, token = token))
            if (response != null) {
                commandsRequiringDragon.clear()
                commandsRequiringDragon.addAll(response.commandsRequiringDragon)
                io.println(response.message)
                response.lines?.forEach { io.println(it) }
                if (response.exitClient) { client.close(); return }
            }
        }
    }
}

fun client(block: ClientDsl.() -> Unit) {
    ClientDsl().apply(block)
}