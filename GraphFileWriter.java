import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class GraphFileWriter {

    private GraphFileWriter() {
    }

    // сохраняет граф в файл
    public static void saveToFile(Graph graph, File file) throws IOException {
        int n = graph.size();
        StringBuilder sb = new StringBuilder();

        // количество вершин
        sb.append(n).append(System.lineSeparator());

        // вершины: id x y
        for (int i = 0; i < n; i++) {
            Graph.Vertex v = graph.getVertex(i);
            sb.append(v.id).append(' ').append((int) v.x).append(' ').append((int) v.y).append(System.lineSeparator());
        }

        // матрица смежности
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j > 0) {
                    sb.append(' ');
                }
                sb.append(formatWeight(graph.matrix[i][j]));
            }
            sb.append(System.lineSeparator());
        }

        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }
    }

    // форматирует вес для записи в файл
    private static String formatWeight(double value) {
        if (Double.isInfinite(value)) {
            return "INF";
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}