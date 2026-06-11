package server

import common.model.*
import java.io.File
import java.time.LocalDateTime
import java.io.BufferedInputStream
import com.google.gson.*
import java.lang.reflect.Type
import common.exceptions.*

/**
 * Класс для работы с файлами коллекции.
 * Отвечает за чтение данных из файла, проверку корректности файла
 * и преобразование JSON в объекты.
 *
 * @property fileName имя файла
 */
class FileManager (private val fileName: String) {

    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter()).create()
    /**
     * Проверяет корректность файла.
     *
     * @param fileName имя файла
     * @return объект File
     * @throws FileNotFoundException если файл не существует
     * @throws FileIsNotRegularException если это не файл
     * @throws FileReadPermissionException если нет прав на чтение
     * @throws FileWritePermissionException если нет прав на запись
     * @throws FileEmptyException если файл пуст
     */
    fun checkFile(fileName: String): File {
        val file = File(fileName)

        if (!file.exists()) {
            throw FileNotFoundException("Файл не существует: $fileName")
        }

        if (!file.isFile) {
            throw FileIsNotRegularException("Это не файл: $fileName")
        }

        if (!file.canRead()) {
            throw FileReadPermissionException("Нет прав на чтение: $fileName")
        }

        if (!file.canWrite()) {
            throw FileWritePermissionException("Нет прав на запись: $fileName")
        }

        if (file.length() == 0L) {
            throw FileEmptyException("Файл пуст: $fileName")
        }

        return file
    }
    /**
     * Считывает содержимое файла.
     *
     * @return текст файла
     */
    fun readfile(): String {
        val file = checkFile(fileName)
        val inputStream = BufferedInputStream(file.inputStream())
        val text = inputStream.bufferedReader().readText()
        inputStream.close()
        return text
    }
    /**
     * Считывает коллекцию из файла и преобразует её из JSON.
     *
     * @return список пар ключ-значение
     * @throws FileFormatException при некорректном формате JSON
     */
    fun readCollection(): List<Pair<Long, Dragon>> {
        val fileJson = readfile().trim() ////убираем пробелы и всякую такую шняжку
        if (fileJson.isEmpty()) return emptyList()

        try {
            val root = gson.fromJson(fileJson, JsonElement::class.java)
            if (root == null) {
                return emptyList()
            }

            if (!root.isJsonObject) {
                throw FileFormatException("Неверный формат ввода")
            }

            return parseObjectFormat(root.asJsonObject)
        }
        catch (e: JsonParseException) {
            throw FileFormatException("Файл не является корректным JSON: ${e.message}")
        }
    }
    /**
     * Преобразует JSON-объект в коллекцию.
     *
     * @param obj JSON объект
     * @return список пар ключ-значение
     */
    private fun  parseObjectFormat (obj: JsonObject): List<Pair<Long, Dragon>> {
        val result = mutableListOf<Pair<Long, Dragon>>()

        for (entry in obj.entrySet()) {
            val keyString = entry.key
            val value = entry.value


            val key = keyString.toLongOrNull()
            if (key == null) {
                throw FileFormatException("Ключ '$keyString' должен быть числом")
            }
            val dragon = gson.fromJson(value, Dragon::class.java)
            result.add(Pair(key, dragon))

        }
        return result
    }
    /**
     * Преобразует JSON-массив в коллекцию.
     *
     * @param arr JSON массив
     * @return список пар ключ-значение
     */
    private fun parseFormat(arr: JsonArray): List<Pair<Long, Dragon>> {
        val result = mutableListOf<Pair<Long, Dragon>>()

        for (element in arr) {
            val obj = element.asJsonObject
            val key = obj.get("key").asLong
            val dragonJson = obj.get("dragon")

            val dragon = gson.fromJson(dragonJson, Dragon::class.java)
            result.add(Pair(key, dragon))
        }
        return result
    }
    /**
     * Адаптер для сериализации и десериализации LocalDateTime в JSON.
     */
    class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        override fun serialize(
            src: LocalDateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src.toString())
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDateTime {
            val text = json?.asString ?: throw JsonParseException("Нет даты")
            return LocalDateTime.parse(text)
        }
    }
}
