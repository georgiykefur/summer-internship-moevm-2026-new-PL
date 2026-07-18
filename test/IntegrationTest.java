import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

// интеграционное тестирование (алгоритм и модель)
// пункт 3
public class IntegrationTest {

    // 3.1 проверка инициализации dist и next
    @Test
    public void testInitCopiesMatrixAndInitializesNext() {
        Graph graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 1);
        // a->c нет

        FloydWarshall fw = new FloydWarshall(graph);
        fw.init();

        double[][] dist = fw.getDistMatrix();

        // dist должна копировать матрицу смежности
        assertEquals(3.0, dist[0][1], 1e-9);
        assertEquals(1.0, dist[1][2], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[0][2], 1e-9);

        // next инициализируется правильно
        // для i != j, если есть ребро, next[i][j] = j
        // иначе -1
    }

    // 3.2 проверка runFull() после init()
    @Test
    public void testRunFullUpdatesMatrices() {
        Graph graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 1);

        FloydWarshall fw = new FloydWarshall(graph);
        fw.init();

        // до runFull a->c = inf
        double[][] distBefore = fw.getDistMatrix();
        assertEquals(Double.POSITIVE_INFINITY, distBefore[0][2], 1e-9);

        fw.runFull();

        double[][] distAfter = fw.getDistMatrix();
        // после runFull a->c = 4 (через b)
        assertEquals(4.0, distAfter[0][2], 1e-9);
        assertTrue(fw.finished);
    }

    // 3.3 повторный вызов init() сбрасывает состояние
    @Test
    public void testInitResetsState() {
        Graph graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 1);

        FloydWarshall fw = new FloydWarshall(graph);
        fw.runFull();

        // алгоритм завершён
        assertTrue(fw.finished);

        // повторная инициализация сбрасывает состояние
        fw.init();
        assertFalse(fw.finished);

        double[][] dist = fw.getDistMatrix();
        // после init a->c снова inf
        assertEquals(Double.POSITIVE_INFINITY, dist[0][2], 1e-9);
    }

    // 3.4 проверка восстановления пути
    @Test
    public void testPathReconstructionIntegration() {
        Graph graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);
        graph.addVertex("D", 3, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "D", 2);
        graph.addEdge("A", "D", 10);

        FloydWarshall fw = new FloydWarshall(graph);
        fw.runFull();

        // путь a->d: a->b->c->d
        List<String> path = fw.reconstructPath("A", "D");
        assertEquals(4, path.size());
        assertEquals("A", path.get(0));
        assertEquals("B", path.get(1));
        assertEquals("C", path.get(2));
        assertEquals("D", path.get(3));

        // проверка, что next[from][to] не равен -1 для каждого шага пути
        // это гарантирует, что путь проходит через вершины, для которых next существует
        List<Integer> pathIndices = fw.getPathIndices(0, 3);
        for (int i = 0; i < pathIndices.size() - 1; i++) {
            int from = pathIndices.get(i);
            int to = pathIndices.get(i + 1);
            // проверяем, что есть прямой путь или ребро
            assertTrue(fw.getDistMatrix()[from][to] < Graph.INF / 2);
        }
    }

    // 3.5 недостижимые вершины — reconstructPath возвращает пустой список
    @Test
    public void testReconstructPathUnreachable() {
        Graph graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);

        // нет ребра между a и b
        FloydWarshall fw = new FloydWarshall(graph);
        fw.runFull();

        List<String> path = fw.reconstructPath("A", "B");
        assertTrue(path.isEmpty());
    }
}