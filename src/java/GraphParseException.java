/**
 * Ошибка разбора файла графа (формат не соответствует контракту проекта).
 * Непроверяемое (unchecked) исключение — GUI сам решает, как показать ошибку
 * пользователю (лог, диалог), не оборачивая каждый вызов в try/catch по цепочке.
 */
public class GraphParseException extends RuntimeException {

    public GraphParseException(String message) {
        super(message);
    }

    public GraphParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
