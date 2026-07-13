public class GuiController {

    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private Graph graph;
    private FloydWarshall floydWarshall;
    private String[] vertexNames;

    public GuiController(GraphPanel gp, MatrixPanel mp, LogPanel lp) {
        this.graphPanel = gp;
        this.matrixPanel = mp;
        this.logPanel = lp;
        this.graph = new Graph();
    }

    public void initialGraph(int[][] matrix) {
        int n = matrix.length;
        int centerX = 300;
        int centerY = 300;
        int radius = 150;

        graphPanel.clearGraph();
        graph = new Graph();

        vertexNames = new String[n];
        for (int i = 0; i < n; i++) {
            vertexNames[i] = String.valueOf((char) ('A' + i));
        }

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            graph.addVertex(vertexNames[i], x, y);
            graphPanel.addVertex(x, y);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] != Integer.MAX_VALUE && i != j) {
                    graph.addEdge(vertexNames[i], vertexNames[j], matrix[i][j]);
                    graphPanel.addEdge(i, j, matrix[i][j]);
                }
            }
        }

        matrixPanel.renderMatrixPanel(matrix);
        graphPanel.repaint();
        logPanel.printLog("Граф загружен. Вершины: " + String.join(", ", vertexNames));
        
        // Вывод списка рёбер в лог
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] != Integer.MAX_VALUE && i != j) {
                    logPanel.printLog("  " + vertexNames[i] + " - " + vertexNames[j] + 
                        " (вес " + matrix[i][j] + ")");
                }
            }
        }
    }

    public void runAlgorithm() {
        if (graph == null || graph.size() == 0) {
            logPanel.printLog("Ошибка: граф не загружен");
            return;
        }

        logPanel.printLog("Запуск алгоритма Флойда-Уоршелла...");
        
        floydWarshall = new FloydWarshall(graph);
        floydWarshall.runFull();

        double[][] dist = floydWarshall.getDistMatrix();

        // Вывод в консолб
        System.out.println("\nМАТРИЦА КРАТЧАЙШИХ РАССТОЯНИЙ");
        System.out.print("     ");
        for (String name : vertexNames) {
            System.out.printf("  %2s  ", name);
        }
        System.out.println();
        for (int i = 0; i < dist.length; i++) {
            System.out.print(vertexNames[i] + "   ");
            for (int j = 0; j < dist.length; j++) {
                if (dist[i][j] >= Graph.INF / 2) {
                    System.out.print("  INF ");
                } else {
                    System.out.printf("  %3.0f ", dist[i][j]);
                }
            }
            System.out.println();
        }

        System.out.println("\nВСЕ КРАТЧАЙШИЕ ПУТИ");
        for (int i = 0; i < vertexNames.length; i++) {
            for (int j = 0; j < vertexNames.length; j++) {
                if (i != j && dist[i][j] < Graph.INF / 2) {
                    java.util.List<String> path = floydWarshall.reconstructPath(vertexNames[i], vertexNames[j]);
                    System.out.printf("  %s - %s : %s (длина = %.0f)%n",
                        vertexNames[i], vertexNames[j],
                        String.join(" - ", path), dist[i][j]);
                }
            }
        }
        System.out.println();
        System.out.println("  Отрицательный цикл: " + (floydWarshall.hasNegativeCycle() ? "Есть" : "Нет"));

        // Вывод в лог
        logPanel.printLog("Алгоритм завершён!");
        logPanel.printLog("Матрица кратчайших расстояний:");
        for (int i = 0; i < dist.length; i++) {
            StringBuilder line = new StringBuilder("  ");
            for (int j = 0; j < dist.length; j++) {
                if (dist[i][j] >= Graph.INF / 2) {
                    line.append("INF ");
                } else {
                    line.append(String.format("%.0f  ", dist[i][j]));
                }
            }
            logPanel.printLog(line.toString());
        }

        logPanel.printLog("Кратчайшие пути:");
        for (int i = 0; i < vertexNames.length; i++) {
            for (int j = 0; j < vertexNames.length; j++) {
                if (i != j && dist[i][j] < Graph.INF / 2) {
                    java.util.List<String> path = floydWarshall.reconstructPath(vertexNames[i], vertexNames[j]);
                    logPanel.printLog("  " + vertexNames[i] + " - " + vertexNames[j] + 
                        " : " + String.join(" - ", path) + 
                        " (длина = " + String.format("%.0f", dist[i][j]) + ")");
                }
            }
        }

        logPanel.printLog("Отрицательный цикл: " + (floydWarshall.hasNegativeCycle() ? "Есть" : "Нет"));

        matrixPanel.renderMatrixPanel(dist);
        logPanel.printLog("Матрица обновлена");
    }
}