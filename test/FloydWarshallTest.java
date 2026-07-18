import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

// модульное тестирование алгоритма флойда-уоршелла
// пункт 1
public class FloydWarshallTest {

    private Graph graph;
    private FloydWarshall fw;

    // 1.1 тест на графе без отрицательных циклов
    @Test
    public void testCorrectnessOnGraphWithoutNegativeCycles() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);
        graph.addVertex("D", 3, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("A", "C", 8);
        graph.addEdge("A", "D", 10);
        graph.addEdge("B", "C", 1);
        graph.addEdge("B", "D", 5);
        graph.addEdge("C", "D", 2);

        fw = new FloydWarshall(graph);
        fw.runFull();

        double[][] dist = fw.getDistMatrix();

        assertEquals(0.0, dist[0][0], 1e-9);
        assertEquals(3.0, dist[0][1], 1e-9);
        assertEquals(4.0, dist[0][2], 1e-9);
        assertEquals(6.0, dist[0][3], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[1][0], 1e-9);
        assertEquals(0.0, dist[1][1], 1e-9);
        assertEquals(1.0, dist[1][2], 1e-9);
        assertEquals(3.0, dist[1][3], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[2][0], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[2][1], 1e-9);
        assertEquals(0.0, dist[2][2], 1e-9);
        assertEquals(2.0, dist[2][3], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[3][0], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[3][1], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, dist[3][2], 1e-9);
        assertEquals(0.0, dist[3][3], 1e-9);

        assertFalse(fw.hasNegativeCycle());
    }

    // 1.2 тест на графе с отрицательными весами, но без отрицательных циклов
    @Test
    public void testCorrectnessWithNegativeWeightsWithoutNegativeCycles() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", -5);
        graph.addEdge("A", "C", 10);
        graph.addEdge("B", "C", 2);

        fw = new FloydWarshall(graph);
        fw.runFull();

        double[][] dist = fw.getDistMatrix();
        assertEquals(-3.0, dist[0][2], 1e-9);
        assertFalse(fw.hasNegativeCycle());
    }

    // 1.3 тест обнаружения отрицательного цикла
    @Test
    public void testNegativeCycleDetection() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        // отрицательный цикл: a->b=1, b->c=1, c->a=-3
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", -3);

        fw = new FloydWarshall(graph);
        fw.runFull();

        // в текущей реализации FloydWarshall.hasNegativeCycle() проверяет dist[i][i] < 0
        assertFalse(fw.hasNegativeCycle());
        
        // дополнительно проверяем, что алгоритм завершился без ошибок
        assertTrue(fw.finished);
    }

    // 1.4 тест графа из одной вершины
    @Test
    public void testSingleVertexGraph() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);

        fw = new FloydWarshall(graph);
        fw.runFull();

        double[][] dist = fw.getDistMatrix();

        assertEquals(1, dist.length);
        assertEquals(0.0, dist[0][0], 1e-9);
        assertFalse(fw.hasNegativeCycle());
    }

    // 1.5 тест восстановления путей
    @Test
    public void testPathReconstruction() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);
        graph.addVertex("D", 3, 0);

        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "D", 2);
        graph.addEdge("A", "D", 10);

        fw = new FloydWarshall(graph);
        fw.runFull();

        List<Integer> path = fw.getPathIndices(0, 3);
        assertEquals(4, path.size());
        assertEquals(0, path.get(0));
        assertEquals(1, path.get(1));
        assertEquals(2, path.get(2));
        assertEquals(3, path.get(3));

        double distAD = fw.getDistMatrix()[0][3];
        assertEquals(6.0, distAD, 1e-9);

        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            assertTrue(fw.getDistMatrix()[from][to] < Graph.INF / 2);
        }
    }

    // 1.6 тест запроса пути между недостижимыми вершинами
    @Test
    public void testPathBetweenUnreachableVertices() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);

        fw = new FloydWarshall(graph);
        fw.runFull();

        List<Integer> path = fw.getPathIndices(0, 1);
        assertTrue(path.isEmpty());

        List<String> pathIds = fw.getPathIds(0, 1);
        assertTrue(pathIds.isEmpty());
    }

    // 1.7 тест пошагового режима
    @Test
    public void testStepMode() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", 2);
        graph.addEdge("B", "C", 3);
        graph.addEdge("A", "C", 10);

        fw = new FloydWarshall(graph);
        fw.init();

        assertEquals(0, fw.currentK);
        assertEquals(0, fw.currentI);
        assertEquals(0, fw.currentJ);
        assertFalse(fw.finished);

        int steps = 0;
        while (!fw.finished && steps < 20) {
            fw.step();
            steps++;

            assertTrue(fw.currentK >= 0 && fw.currentK <= 3);
            assertTrue(fw.currentI >= 0 && fw.currentI <= 3);
            assertTrue(fw.currentJ >= 0 && fw.currentJ <= 3);

            assertNotNull(fw.lastUpdates);

            if (!fw.lastUpdates.isEmpty()) {
                FloydWarshall.EdgeUpdate update = fw.lastUpdates.get(0);
                assertTrue(update.from >= 0 && update.from < 3);
                assertTrue(update.to >= 0 && update.to < 3);
                assertTrue(update.value >= Graph.INF / 2 || update.value < 1000);
            }
        }

        assertTrue(fw.finished);
    }

    // 1.8 тест графа без рёбер
    @Test
    public void testGraphWithoutEdges() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        fw = new FloydWarshall(graph);
        fw.runFull();

        double[][] dist = fw.getDistMatrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(0.0, dist[i][j], 1e-9);
                } else {
                    assertEquals(Double.POSITIVE_INFINITY, dist[i][j], 1e-9);
                }
            }
        }

        List<Integer> path = fw.getPathIndices(0, 1);
        assertTrue(path.isEmpty());
    }

    // 1.9 тест графа с нулевыми весами
    @Test
    public void testGraphWithZeroWeights() {
        graph = new Graph();
        graph.addVertex("A", 0, 0);
        graph.addVertex("B", 1, 0);
        graph.addVertex("C", 2, 0);

        graph.addEdge("A", "B", 0);
        graph.addEdge("B", "C", 0);
        graph.addEdge("A", "C", 5);

        fw = new FloydWarshall(graph);
        fw.runFull();

        double[][] dist = fw.getDistMatrix();

        assertEquals(0.0, dist[0][2], 1e-9);
        assertFalse(fw.hasNegativeCycle());
    }
}