package client

import common.model.Dragon
import common.model.Coordinates
import common.model.DragonType
import common.model.DragonCharacter
import common.model.DragonHead
import java.time.LocalDateTime
import java.io.File
import java.util.Scanner
import java.io.PrintStream
/**
 * Класс для ввода и вывода данных.
 * Используется для работы с консолью и файлами.
 */
class IOManager {
    private var scanner: Scanner = Scanner(System.`in`)
    private val outStream: PrintStream = PrintStream(System.out, true, "UTF-8")
    /**
     * Выводит сообщение в консоль.
     * @param message текст сообщения
     */
    fun println(message: String) {
        kotlin.io.println(message)
    }
    /**
     * Выводит сообщение в консоль без перевода строки.
     * @param message текст сообщения
     */
    fun print(message: String) {
        outStream.print(message)
    }
    /**
     * Сбрасывает буфер вывода.
     */
    fun flush() {
        outStream.flush()
    }
    /**
     * Считывает строку из текущего источника ввода.
     * @return введённая строка
     */
    var currentFile: String? = null

    fun setFileInput(file: java.io.File) {
        currentFile = file.name
        scanner = Scanner(file)
    }

    fun readLine(): String {
        return if (scanner.hasNextLine()) {
            scanner.nextLine()
        } else {
            currentFile = null
            scanner = Scanner(System.`in`)
            ""
        }
    }
    fun readLong(message: String): Long {
        while (true) {
            println(message)
            try { return readLine().toLong() }
            catch (e: NumberFormatException) { println("Неверный ввод, должно быть Long") }
        }
    }

    fun readInt(message: String): Int {
        while (true) {
            println(message)
            try { return readLine().toInt() }
            catch (e: NumberFormatException) { println("Неверный ввод, должно быть Int") }
        }
    }

    fun readFloat(message: String): Float {
        while (true) {
            println(message)
            try { return readLine().toFloat() }
            catch (e: NumberFormatException) { println("Неверный ввод, должно быть Float") }
        }
    }

    fun readDouble(message: String): Double {
        while (true) {
            println(message)
            try { return readLine().toDouble() }
            catch (e: NumberFormatException) { println("Неверный ввод, должно быть Double") }
        }
    }
    /**
     * Создаёт объект Dragon на основе пользовательского ввода.
     * @param id идентификатор
     * @return созданный объект Dragon
     */
    fun createDragon(id: Int): Dragon {
        println("Введите имя")
        val nameDragon = readLine()

        val x = readFloat("Введите координату x")
        val y = readLong("Введите координату y")
        val age = readLong("Введите возраст")
        val weight = readDouble("Введите вес")

        var type: DragonType
        while (true) {
            println("Выберете тип дракона: water, underground, air, fire")
            try {
                type = DragonType.valueOf(readLine().uppercase())
                break
            } catch (e: Exception) {
                println("Неверный тип дракона")
            }
        }

        var character: DragonCharacter
        while (true) {
            println("Выберете характер дракона: wise, good, chaotic, chaotic_evil, fickle")
            try {
                character = DragonCharacter.valueOf(readLine().uppercase())
                break
            } catch (e: Exception) {
                println("Неверный характер дракона")
            }
        }

        println("Создать голову? yes/no")
        val head: DragonHead? = if (readLine().lowercase() == "yes") {
            val eyesCount = readInt("Введите количество глаз")
            val toothCount = readDouble("Введите количество зубов")
            DragonHead(eyesCount, toothCount)
        } else null

        return Dragon(
            id = id,
            name = nameDragon,
            coordinates = Coordinates(x, y),
            creationDate = LocalDateTime.now(),
            age = age,
            weight = weight,
            type = type,
            character = character,
            head = head
        )
    }
}