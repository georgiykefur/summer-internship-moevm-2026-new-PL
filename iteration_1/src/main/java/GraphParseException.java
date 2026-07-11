/**
 * Ошибка разбора файла графа (формат не соответствует контракту проекта).
 * Непроверяемое (unchecked) исключение — намеренно, чтобы вызывающий GUI-код
 * мог решить сам, как показать ошибку пользователю (лог, диалог и т.п.),
 * не заставляя каждый вызов оборачивать в try/catch по цепочке.
 */
public class GraphParseException extends RuntimeException {

    public GraphParseException(String message) {
        super(message);
    }

    public GraphParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
