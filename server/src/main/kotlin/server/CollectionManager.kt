package server

import common.model.Dragon
import java.time.LocalDateTime
import java.util.Collections

/**
 * Класс для управления коллекцией объектов Dragon.
 * Отвечает за хранение, изменение и обработку элементов коллекции.
 *
 * @property time время инициализации коллекции
 */
class CollectionManager(val time: LocalDateTime, val db: DatabaseManager) {

    val storage: MutableMap<Long, Dragon> = Collections.synchronizedMap(mutableMapOf())

    fun loadFromDatabase() {
        val dragons = db.loadDragons()
        storage.putAll(dragons)
    }

    fun loadFromDatabaseFresh(): Map<Long, Dragon> = db.loadDragons()

    /**
     * Возвращает количество элементов в коллекции.
     * @return размер коллекции
     */
    fun size(): Int = storage.size

    /**
     * Выводит все элементы коллекции.
     */
    fun showAll(): List<String> {
        if (storage.isEmpty()) return listOf("Коллекция пустая")
        return storage.entries.flatMap { listOf("Ключ = ${it.key}", "Значение = ${it.value}") }
    }

    fun clear(owner: String): String {
        val count = db.clearDragons(owner)
        storage.entries.removeIf { it.value.owner == owner }
        return "Удалено элементов: $count"
    }

    /**
     * Удаляет элемент по заданному ключу.
     * @param key ключ элемента
     */
    fun removeByKey(key: Long, owner: String): String {
        val dragon = storage[key] ?: return "Ключ не найден"
        if (dragon.owner != owner) return "Нет прав для удаления этого элемента"
        val deleted = db.deleteDragon(key, owner)
        if (deleted) storage.remove(key)
        return if (deleted) "Элемент удалён" else "Ошибка удаления"
    }

    fun insert(dragon: Dragon, owner: String): String {
        val id = db.insertDragon(dragon, owner)
        val saved = dragon.copy(id = id.toInt(), owner = owner)
        storage[id] = saved
        return "Элемент добавлен с id=$id"
    }

    /**
     * Обновляет элемент по его id.
     * @param id идентификатор элемента
     * @param newDragon новый объект
     */
    fun updateById(id: Long, newDragon: Dragon, owner: String): String {
        val entry = storage.entries.find { it.value.id.toLong() == id }
            ?: return "Элемент с таким id не найден"
        if (entry.value.owner != owner) return "Нет прав для изменения этого элемента"
        val updated = newDragon.copy(id = id.toInt(), creationDate = entry.value.creationDate, owner = owner)
        val success = db.updateDragon(updated, owner)
        if (success) storage[entry.key] = updated
        return if (success) "Элемент обновлён" else "Ошибка обновления"
    }

    /**
     * Удаляет элементы с ключом больше заданного.
     * @param key ключ для сравнения
     */
    fun removeGreaterKey(key: Long, owner: String): String {
        val count = db.deleteDragonsGreaterKey(key, owner)
        storage.entries.removeIf { it.key > key && it.value.owner == owner }
        return "Удалено элементов: $count"
    }

    fun removeGreater(param: String, value: Double, owner: String): String {
        val fieldMap: Map<String, (Dragon) -> Double> = mapOf(
            "id" to { it.id.toDouble() },
            "x" to { it.coordinates.x.toDouble() },
            "y" to { it.coordinates.y.toDouble() },
            "toothCount" to { it.head?.toothCount ?: 0.0 },
            "age" to { it.age.toDouble() },
            "weight" to { it.weight },
            "eyesCount" to { (it.head?.eyesCount ?: 0).toDouble() }
        )
        val selector = fieldMap[param] ?: return "Неверный параметр"
        val toRemove = storage.filter { (_, d) -> d.owner == owner && selector(d) > value }
        toRemove.forEach { (k, _) ->
            db.deleteDragon(k, owner)
            storage.remove(k)
        }
        return if (toRemove.isEmpty()) "Нет элементов, превышающих заданное значение" else "Удалено элементов: ${toRemove.size}"
    }

    /**
     * Заменяет значение элемента, если новое больше текущего.
     */
    fun replaceIfGreater(key: Long, param: String, value: Double, owner: String): String {
        val current = storage[key] ?: return "Элемент с таким ключом не найден"
        if (current.owner != owner) return "Нет прав для изменения этого элемента"

        val updated = when (param) {
            "x" -> if (value > current.coordinates.x) current.copy(coordinates = current.coordinates.copy(x = value.toFloat())) else null
            "y" -> if (value > current.coordinates.y) current.copy(coordinates = current.coordinates.copy(y = value.toLong())) else null
            "age" -> if (value > current.age) current.copy(age = value.toLong()) else null
            "weight" -> if (value > current.weight) current.copy(weight = value) else null
            else -> return "Неверный параметр"
        } ?: return "Новое значение не больше старого, замена не выполнена"

        val success = db.updateDragon(updated, owner)
        if (success) storage[key] = updated
        return if (success) "Элемент успешно заменён" else "Ошибка обновления"
    }

    /**
     * Выводит элементы коллекции, отсортированные по выбранному параметру.
     */
    fun printAscending(param: String): List<String> {
        if (storage.isEmpty()) return listOf("Элементы не найдены")
        val sortedList = when (param) {
            "id" -> storage.values.sortedBy { it.id }
            "x" -> storage.values.sortedBy { it.coordinates.x }
            "y" -> storage.values.sortedBy { it.coordinates.y }
            "creationDate" -> storage.values.sortedBy { it.creationDate }
            "age" -> storage.values.sortedBy { it.age }
            "weight" -> storage.values.sortedBy { it.weight }
            "eyesCount" -> storage.values.sortedBy { it.head?.eyesCount }
            "toothCount" -> storage.values.sortedBy { it.head?.toothCount }
            else -> return listOf("Неверный параметр")
        }
        return sortedList.map { it.toString() }
    }

    /**
     * Фильтрует элементы по префиксу имени.
     * @param prefix начало имени
     */
    fun filterStartsWithName(prefix: String): List<String> {
        val filtered = storage.values.filter { it.name.startsWith(prefix) }
        return if (filtered.isEmpty()) listOf("Элементы не найдены") else filtered.map { it.toString() }
    }

    /**
     * Группирует элементы по id и выводит количество в каждой группе.
     */
    fun groupCountingById(): List<String> {
        val grouped = storage.values.groupingBy { it.id }.eachCount()
        return if (grouped.isEmpty()) listOf("Коллекция пуста") else grouped.map { (id, count) -> "ID: $id -> количество: $count" }
    }

    fun info(): String = "Тип: Hashtable, Время инициализации: $time, Размер: ${storage.size}"
}