package common.exceptions
/**
 * Исключение, возникающее если файл не найден.
 */
class FileNotFoundException(message: String) : Exception(message)
/**
 * Исключение, возникающее при отсутствии прав на чтение файла.
 */
class FileReadPermissionException(message: String) : Exception(message)
/**
 * Исключение, возникающее при отсутствии прав на запись в файл.
 */
class FileWritePermissionException(message: String) : Exception(message)
/**
 * Исключение, возникающее если указанный путь не является файлом.
 */
class FileIsNotRegularException(message: String) : Exception(message)
/**
 * Исключение, возникающее если файл пуст.
 */
class FileEmptyException(message: String) : Exception(message)
/**
 * Исключение, возникающее при некорректном формате файла (например, неверный JSON).
 */
class FileFormatException(message: String) : Exception(message)