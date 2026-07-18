import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class GraphFileLoader {

    private GraphFileLoader() {
    }

    // загружает граф из файла
    public static Graph loadFromFile(File file) throws IOException {
        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        // убираем пустые строки
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

        // читаем вершины: id x y
        requireEnoughLines(lines, cursor + n, "координат вершин", file);
        String[] ids = new String[n];
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            String[] tokens = splitTokens(lines.get(cursor + i));
            if (tokens.length != 3) {
                throw new GraphParseException(
                        "Строка вершины " + i + " должна содержать 3 элемента (id x y), а содержит "
                                + tokens.length + ": \"" + lines.get(cursor + i) + "\" (файл " + file.getName() + ")");
            }
            ids[i] = tokens[0];
            x[i] = parseIntToken(tokens[1], "координата x вершины " + i, file);
            y[i] = parseIntToken(tokens[2], "координата y вершины " + i, file);
        }
        cursor += n;

        // читаем матрицу смежности
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

        // создаём граф
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addVertex(ids[i], x[i], y[i]);
        }
        for (int i = 0; i < n; i++) {
            System.arraycopy(weights[i], 0, graph.matrix[i], 0, n);
        }

        return graph;
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