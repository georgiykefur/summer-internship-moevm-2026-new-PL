import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphFileLoaderTest {

    @TempDir
    Path tempDir;

    private File writeFile(String name, String content) throws IOException {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content);
        return path.toFile();
    }

    @Test
    void loadsValidSampleGraph() throws IOException {
        String content = """
                4
                50 50
                200 50
                200 200
                50 200
                0 3 INF 7
                8 0 2 INF
                5 INF 0 1
                2 INF INF 0
                """;
        File file = writeFile("sample.txt", content);

        Graph graph = GraphFileLoader.loadFromFile(file);

        assertEquals(4, graph.vertexCount());
        assertEquals(50, graph.getX(0));
        assertEquals(50, graph.getY(0));
        assertEquals(200, graph.getX(2));
        assertEquals(200, graph.getY(2));

        assertEquals(0.0, graph.getDistance(0, 0));
        assertEquals(3.0, graph.getDistance(0, 1));
        assertTrue(Double.isInfinite(graph.getDistance(0, 2)));
        assertEquals(7.0, graph.getDistance(0, 3));
        assertEquals(1.0, graph.getDistance(2, 3));
    }

    @Test
    void parsesNegativeAndDecimalWeights() throws IOException {
        String content = """
                2
                0 0
                10 10
                0 -2.5
                3.5 0
                """;
        File file = writeFile("weights.txt", content);

        Graph graph = GraphFileLoader.loadFromFile(file);

        assertEquals(-2.5, graph.getDistance(0, 1));
        assertEquals(3.5, graph.getDistance(1, 0));
    }

    @Test
    void throwsWhenFirstLineNotANumber() throws IOException {
        File file = writeFile("bad_n.txt", "abc\n0 0\n0\n");

        assertThrows(GraphParseException.class, () -> GraphFileLoader.loadFromFile(file));
    }

    @Test
    void throwsWhenVertexLineHasWrongTokenCount() throws IOException {
        String content = """
                1
                0 0 0
                0
                """;
        File file = writeFile("bad_vertex.txt", content);

        assertThrows(GraphParseException.class, () -> GraphFileLoader.loadFromFile(file));
    }

    @Test
    void throwsWhenMatrixRowHasWrongLength() throws IOException {
        String content = """
                2
                0 0
                10 10
                0 5
                1
                """;
        File file = writeFile("bad_matrix.txt", content);

        assertThrows(GraphParseException.class, () -> GraphFileLoader.loadFromFile(file));
    }

    @Test
    void throwsWhenFileEndsTooEarly() throws IOException {
        String content = """
                3
                0 0
                10 10
                """;
        File file = writeFile("too_short.txt", content);

        assertThrows(GraphParseException.class, () -> GraphFileLoader.loadFromFile(file));
    }
}
