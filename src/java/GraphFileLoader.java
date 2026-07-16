import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Загрузка графа из файла и парсинг в модель — задача Харченко Я.К. на
 * итерации 1. Парсит напрямую в {@link Graph} (модель Гриценко К.В.), чтобы
 * результат сразу был совместим с алгоритмом ({@link FloydWarshall}) и
 * отрисовкой ({@code GraphPanel}) без промежуточного конвертирования.
 *
 * Формат файла (контракт проекта, docs/specification.md §2):
 * <pre>
 * N
 * x1 y1
 * ...
 * xN yN
 * d00 d01 ... d0,N-1
 * ...
 * dN-1,0 ... dN-1,N-1
 * </pre>
 * {@code N} — количество вершин, координаты — целые числа, матрица —
 * веса рёбер (целые/вещественные, могут быть отрицательными или нулевыми),
 * {@code INF} — ребро отсутствует.
 */
public final class GraphFileLoader {

    private GraphFileLoader() {
    }

    /**
     * Читает файл и строит {@link Graph} согласно контракту формата.
     * Вершинам присваиваются буквенные идентификаторы, как и
     * в {@code GuiController.initialGraph}, чтобы не расходиться со схемой
     * именования, уже принятой в проекте.
     *
     * @throws IOException          если файл не удалось прочитать
     * @throws GraphParseException  если содержимое не соответствует контракту формата
     */
    public static Graph loadFromFile(File file) throws IOException {
        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

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
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            String[] tokens = splitTokens(lines.get(cursor + i));
            if (tokens.length != 2) {
                throw new GraphParseException(
                        "Строка координат вершины " + i + " должна содержать 2 числа (x y), а содержит "
                                + tokens.length + ": \"" + lines.get(cursor + i) + "\" (файл " + file.getName() + ")");
            }
            x[i] = parseIntToken(tokens[0], "координата x вершины " + i, file);
            y[i] = parseIntToken(tokens[1], "координата y вершины " + i, file);
        }
        cursor += n;

        requireEnoughLines(lines, cursor + n, "строк матрицы весов", file);
        double[][] weights = new double[n][n];
        for (int i = 0; i < n; i++) {
            String[] tokens = splitTokens(lines.get(cursor + i));
            if (tokens.length != n) {
                throw new GraphParseException(
                        "Строка " + i + " матрицы весов должна содержать " + n + " чисел, а содержит "
                                + tokens.length + ": \"" + lines.get(cursor + i) + "\" (файл " + file.getName() + ")");
            }
            for (int j = 0; j < n; j++) {
                weights[i][j] = parseWeight(tokens[j], i, j, file);
            }
        }

        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addVertex(vertexId(i), x[i], y[i]);
        }
        // addVertex() уже пересобрал матрицу нужного размера (см. Graph.rebuildMatrix) —
        // теперь просто проставляем в неё веса из файла как есть, включая диагональ.
        for (int i = 0; i < n; i++) {
            System.arraycopy(weights[i], 0, graph.matrix[i], 0, n);
        }

        return graph;
    }

    private static String vertexId(int index) {
        return index < 26 ? String.valueOf((char) ('A' + index)) : ("V" + index);
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

    private static int parseIntToken(String token, String what, File file) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            throw new GraphParseException(
                    what + " должна быть целым числом, получено: \"" + token + "\" (файл " + file.getName() + ")", ex);
        }
    }

    private static double parseWeight(String token, int i, int j, File file) {
        if (token.equalsIgnoreCase("INF")) {
            return Graph.INF;
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
