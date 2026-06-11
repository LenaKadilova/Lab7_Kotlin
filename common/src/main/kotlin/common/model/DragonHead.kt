package common.model
import java.io.Serializable
/**
 * Класс головы дракона.
 *
 * @property eyesCount количество глаз
 * @property toothCount количество зубов
 */
data class DragonHead (
    var eyesCount: Int,
    var toothCount: Double // не null
) : Serializable