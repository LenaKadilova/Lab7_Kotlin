package client

import common.Request

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    val io = IOManager()
    val client = Client("192.168.10.80", 12345, io)
    val executingScripts = mutableSetOf<String>()
    val commandsRequiringDragon = mutableSetOf<String>()

    var login: String
    var passwordHash: String

    while (true) {
        io.println("Введите 'login' для входа или 'register' для регистрации:")
        io.print("> ")
        io.flush()
        when (io.readLine().trim()) {
            "login" -> {
                io.println("Логин:")
                login = io.readLine().trim()
                io.println("Пароль:")
                val password = io.readLine().trim()
                passwordHash = client.hashPassword(password)

                val response = client.send(Request("help", null, null, login, passwordHash))
                if (response == null || response.message.startsWith("Ошибка авторизации")) {
                    io.println("Неверный логин или пароль")
                    continue
                }
                io.println("Вход выполнен")
                commandsRequiringDragon.addAll(response.commandsRequiringDragon)
                break
            }
            "register" -> {
                io.println("Логин:")
                login = io.readLine().trim()
                io.println("Пароль:")
                val password = io.readLine().trim()
                passwordHash = client.hashPassword(password)

                val response = client.send(Request("register", null, null, login, passwordHash))
                if (response == null || response.message.startsWith("Пользователь с таким")) {
                    io.println(response?.message ?: "Сервер недоступен")
                    continue
                }
                io.println(response.message)
                break
            }
            else -> io.println("Неверная команда")
        }
    }

    io.println("Клиент запущен. Введите команду:")

    while (true) {
        io.print("> ")
        io.flush()

        val line = io.readLine()
        if (io.currentFile == null) {
            executingScripts.clear()
        }
        if (line.isBlank()) continue

        val parts = line.trim().split(Regex("\\s+"))
        val commandName = parts[0]
        val argument = parts.drop(1).joinToString(" ").ifEmpty { null }

        if (commandName == "execute_script") {
            val fileName = argument ?: run {
                io.println("Укажите файл")
                continue
            }
            if (executingScripts.contains(fileName)) {
                io.println("Обнаружена рекурсия! Скрипт уже выполняется.")
                continue
            }
            val file = java.io.File(fileName)
            if (!file.exists()) {
                io.println("Файл не найден: $fileName")
                continue
            }
            executingScripts.add(fileName)
            io.setFileInput(file)
            continue
        }

        val dragon = if (commandName in commandsRequiringDragon) {
            val key = argument?.toLongOrNull()
            if (key == null) {
                io.println("Необходимо указать числовой ключ")
                continue
            }
            io.createDragon(0)
        } else {
            null
        }

        val response = client.send(Request(commandName, argument, dragon, login, passwordHash))

        if (response != null) {
            commandsRequiringDragon.clear()
            commandsRequiringDragon.addAll(response.commandsRequiringDragon)

            io.println(response.message)
            response.lines?.forEach { io.println(it) }

            if (response.exitClient) {
                client.close()
                return
            }
        }
    }
}