import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Загрузка графа из файла и парсинг в модель ({@link Graph}) — задача
 * Харченко Я.К. на итерации 1 (перенесена сюда с итерации 0 по решению
 * преподавателя от 09.07.2026, см. журнал изменений в {@code docs/specification.md}).
 *
 * Формат файла (контракт проекта, не менять без согласования со всей командой):
 * <pre>
 * N
 * x1 y1
 * x2 y2
 * ...
 * xN yN
 * d00 d01 ... d0,N-1
 * ...
 * dN-1,0 ... dN-1,N-1
 * </pre>
 * {@code N} — количество вершин, {@code xi yi} — координаты вершины i (целые числа),
 * матрица {@code N×N} — веса рёбер (целые или вещественные, могут быть отрицательными
 * или нулевыми), {@code INF} — ребро отсутствует. Эталон — {@code data/sample_graph.txt}.
 */
public final class GraphFileLoader {

    private GraphFileLoader() {
        // утилитный класс, инстанцировать не нужно
    }

    /**
     * Читает файл и парсит его в {@link Graph} согласно контракту формата.
     *
     * @param file файл графа
     * @return модель графа
     * @throws IOException           если файл не удалось прочитать (не найден, нет прав и т.п.)
     * @throws GraphParseException   если содержимое файла не соответствует контракту формата
     */
    public static Graph loadFromFile(File file) throws IOException {
        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        // Пустые строки в контракте не предусмотрены, но игнорируем их защитно —
        // это не меняет смысл формата и снижает хрупкость парсера.
        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            if (!line.isBlank()) {
                lines.add(line.trim());
            }
        }

        if (lines.isEmpty()) {
            throw new GraphParseException("Файл графа пуст: " + file.getName());
        }

        int cursor = 0;
        int n = parseVertexCount(lines.get(cursor), file);
        cursor++;

        requireEnoughLines(lines, cursor + n, "координат вершин", file);
        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            String[] tokens = splitTokens(lines.get(cursor + i));
            if (tokens.length != 2) {
                throw new GraphParseException(
                        "Строка координат вершины " + i + " должна содержать 2 числа (x y), а содержит "
                                + tokens.length + ": \"" + lines.get(cursor + i) + "\" (файл " + file.getName() + ")");
            }
            x[i] = parseInt(tokens[0], "координата x вершины " + i, file);
            y[i] = parseInt(tokens[1], "координата y вершины " + i, file);
        }
        cursor += n;

        requireEnoughLines(lines, cursor + n, "строк матрицы весов", file);
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            String[] tokens = splitTokens(lines.get(cursor + i));
            if (tokens.length != n) {
                throw new GraphParseException(
                        "Строка " + i + " матрицы весов должна содержать " + n + " чисел, а содержит "
                                + tokens.length + ": \"" + lines.get(cursor + i) + "\" (файл " + file.getName() + ")");
            }
            for (int j = 0; j < n; j++) {
                dist[i][j] = parseWeight(tokens[j], i, j, file);
            }
        }

        return new Graph(x, y, dist);
    }

    private static int parseVertexCount(String line, File file) {
        try {
            int n = Integer.parseInt(line);
            if (n <= 0) {
                throw new GraphParseException(
                        "Количество вершин N должно быть положительным, получено " + n + " (файл " + file.getName() + ")");
            }
            return n;
        } catch (NumberFormatException ex) {
            throw new GraphParseException(
                    "Первая строка файла должна содержать целое число N (количество вершин), получено: \""
                            + line + "\" (файл " + file.getName() + ")", ex);
        }
    }

    private static int parseInt(String token, String what, File file) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            throw new GraphParseException(
                    what + " должна быть целым числом, получено: \"" + token + "\" (файл " + file.getName() + ")", ex);
        }
    }

    private static double parseWeight(String token, int i, int j, File file) {
        if (token.equalsIgnoreCase("INF")) {
            return Double.POSITIVE_INFINITY;
        }
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException ex) {
            throw new GraphParseException(
                    "Вес ребра [" + i + "][" + j + "] должен быть числом или INF, получено: \"" + token
                            + "\" (файл " + file.getName() + ")", ex);
        }
    }

    private static String[] splitTokens(String line) {
        return line.trim().split("\\s+");
    }

    private static void requireEnoughLines(List<String> lines, int required, String what, File file) {
        if (lines.size() < required) {
            throw new GraphParseException(
                    "Файл графа обрывается раньше времени: не хватает строк для " + what
                            + " (файл " + file.getName() + ")");
        }
    }
}
