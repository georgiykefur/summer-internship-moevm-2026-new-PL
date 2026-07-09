import java.util.List;

/**
 * Тестовый запуск алгоритма с выводом всех путей.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("   Алгоритм Флойда-Уоршелла\n");

        Graph graph = new Graph();
        
        graph.addVertex("A", 100, 100);
        graph.addVertex("B", 300, 100);
        graph.addVertex("C", 200, 300);

        graph.addEdge("A", "B", 2);
        graph.addEdge("B", "C", 3);
        graph.addEdge("A", "C", 10);
        graph.addEdge("C", "A", 1);

        // Выводим информацию о графе
        System.out.println("Вершины: A, B, C");
        System.out.println("Рёбра:");
        System.out.println("  A - B  (вес 2)");
        System.out.println("  B - C  (вес 3)");
        System.out.println("  A - C  (вес 10)");
        System.out.println("  C - A  (вес 1)");
        System.out.println();

        FloydWarshall fw = new FloydWarshall(graph);
        fw.runFull();

        // Выводим матрицу кратчайших расстояний
        double[][] dist = fw.getDistMatrix();
        String[] names = {"A", "B", "C"};
        
        // Заголовок
        System.out.print("     ");
        for (String name : names) {
            System.out.printf("  %2s  ", name);
        }
        System.out.println();
        
        // Строки матрицы
        for (int i = 0; i < dist.length; i++) {
            System.out.print(names[i] + "   ");
            for (int j = 0; j < dist.length; j++) {
                if (dist[i][j] >= Graph.INF / 2) {
                    System.out.print("  INF ");
                } else {
                    System.out.printf("  %3.0f ", dist[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println();

        // Выводим все кратчайшие пути
        int pathCount = 0;
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                if (i != j && dist[i][j] < Graph.INF / 2) {
                    List<String> path = fw.reconstructPath(names[i], names[j]);
                    System.out.printf("  %s - %s : ", names[i], names[j]);
                    System.out.print(String.join(" - ", path));
                    System.out.printf("  (длина = %.0f)%n", dist[i][j]);
                    pathCount++;
                }
            }
        }
        
        if (pathCount == 0) {
            System.out.println("  (нет достижимых путей)");
        }
        System.out.println();

        // Проверка на отрицательные циклы
        System.out.println("  Отрицательный цикл: " + (fw.hasNegativeCycle() ? "Есть" : "Нет"));
        
        // Пошаговый режим
        FloydWarshall fwStep = new FloydWarshall(graph);
        fwStep.init();
        
        int stepNum = 0;
        while (!fwStep.finished && stepNum < 5) {
            stepNum++;
            boolean improved = fwStep.step();
            
            String fromName = names[fwStep.currentI];
            String toName = names[fwStep.currentJ];
            String kName = names[fwStep.currentK];
            
            System.out.printf("Шаг %d: k=%s, i=%s, j=%s  ", 
                              stepNum, kName, fromName, toName);
            
            if (improved) {
                System.out.println("Улучшение");
            } else {
                System.out.println("Без изменений");
            }
        }
    }
}