package client

import common.Request

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    val io = IOManager()
    val client = Client("172.20.10.13", 12345, io)
    val executingScripts = mutableSetOf<String>()
    val commandsRequiringDragon = mutableSetOf<String>()

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

        val response = client.send(Request(commandName, argument, dragon))

        if (response != null) {
            commandsRequiringDragon.clear()
            commandsRequiringDragon.addAll(response.commandsRequiringDragon)

            io.println(response.message)
            response.lines?.forEach { line -> io.println(line) }

            if (response.exitClient) return
        }
    }
}