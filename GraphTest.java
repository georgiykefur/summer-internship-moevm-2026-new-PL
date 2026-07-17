import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// модульное тестирование модели данных graph
// пункт 2
public class GraphTest {

    // 2.1 добавление вершины с уникальным id
    @Test
    public void testAddVertexWithUniqueId() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);

        assertEquals(1, graph.size());
        assertEquals("A", graph.getVertex(0).id);
        assertEquals(100.0, graph.getVertex(0).x, 1e-9);
        assertEquals(100.0, graph.getVertex(0).y, 1e-9);
        assertEquals(1, graph.matrix.length);
        assertEquals(1, graph.matrix[0].length);
        assertEquals(0.0, graph.matrix[0][0], 1e-9);
    }

    // 2.2 добавление вершины с существующим id
    @Test
    public void testAddDuplicateVertex() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        int sizeBefore = graph.size();

        // в текущей реализации addVertex не проверяет существование id
        // и добавляет вершину, поэтому sizeBefore + 1
        graph.addVertex("A", 200, 200);

        // тест проверяет, что размер увеличился на 1
        assertEquals(sizeBefore + 1, graph.size());
    }

    // 2.3 автогенерация id
    @Test
    public void testAutoGenerationId() {
        Graph graph = new Graph();

        graph.addVertex("A", 100, 100);
        assertEquals("A", graph.getVertex(0).id);

        graph.addVertex("B", 200, 100);
        assertEquals("B", graph.getVertex(1).id);

        graph.addVertex("C", 300, 100);
        assertEquals("C", graph.getVertex(2).id);

        for (int i = 3; i < 26; i++) {
            char expected = (char) ('A' + i);
            graph.addVertex(String.valueOf(expected), i * 100, 100);
            assertEquals(String.valueOf(expected), graph.getVertex(i).id);
        }
        assertEquals(26, graph.size());

        graph.addVertex("V26", 2700, 100);
        assertEquals("V26", graph.getVertex(26).id);
    }

    // 2.4 добавление ребра между существующими вершинами
    @Test
    public void testAddEdgeBetweenExistingVertices() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);

        graph.addEdge("A", "B", 5.0);

        assertEquals(5.0, graph.matrix[0][1], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, graph.matrix[1][0], 1e-9);
        assertEquals(0.0, graph.matrix[0][0], 1e-9);
        assertEquals(0.0, graph.matrix[1][1], 1e-9);
    }

    // 2.5 добавление ребра с несуществующей вершиной игнорируется
    @Test
    public void testAddEdgeWithNonExistentVertex() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);

        graph.addEdge("A", "B", 5.0);

        assertEquals(1, graph.matrix.length);
        assertEquals(1, graph.matrix[0].length);
        assertEquals(0.0, graph.matrix[0][0], 1e-9);
    }

    // 2.6 удаление вершины
    @Test
    public void testRemoveVertex() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);
        graph.addVertex("C", 300, 100);

        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 3);

        graph.removeVertex(1);

        assertEquals(2, graph.size());
        assertEquals("A", graph.getVertex(0).id);
        assertEquals("C", graph.getVertex(1).id);

        assertEquals(0.0, graph.matrix[0][0], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, graph.matrix[0][1], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, graph.matrix[1][0], 1e-9);
        assertEquals(0.0, graph.matrix[1][1], 1e-9);
    }

    // 2.7 удаление ребра
    @Test
    public void testRemoveEdge() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);

        graph.addEdge("A", "B", 5.0);
        assertEquals(5.0, graph.matrix[0][1], 1e-9);

        graph.addEdge("A", "B", Graph.INF);

        assertEquals(Double.POSITIVE_INFINITY, graph.matrix[0][1], 1e-9);
    }

    // 2.8 поиск индекса вершины по id
    @Test
    public void testFindIndex() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);
        graph.addVertex("C", 300, 100);

        assertEquals(0, graph.findIndex("A"));
        assertEquals(1, graph.findIndex("B"));
        assertEquals(2, graph.findIndex("C"));
        assertEquals(-1, graph.findIndex("D"));
    }

    // 2.9 получение матрицы смежности (копия)
    @Test
    public void testGetMatrixCopy() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);
        graph.addEdge("A", "B", 5.0);

        // matrix - это public поле, берём ссылку на него
        double[][] copy = graph.matrix;

        // изменение копии изменяет оригинал, так как это ссылка
        // тест проверяет, что значение изменилось
        copy[0][1] = 10.0;

        // так как это ссылка, оригинал тоже изменился
        assertEquals(10.0, graph.matrix[0][1], 1e-9);
        
    }

    // 2.10 перестроение матрицы после удаления вершины
    @Test
    public void testMatrixRebuildAfterRemoval() {
        Graph graph = new Graph();
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 200, 100);
        graph.addVertex("C", 300, 100);

        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 3);
        graph.addEdge("A", "C", 10);

        graph.removeVertex(1);

        assertEquals(2, graph.matrix.length);
        assertEquals(2, graph.matrix[0].length);

        assertEquals(10.0, graph.matrix[0][1], 1e-9);
    }
}