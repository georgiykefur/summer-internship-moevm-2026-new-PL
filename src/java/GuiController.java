public class GuiController {

    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private Graph graph;
    private FloydWarshall floydWarshall;
    private String[] vertexNames;

    private StepHistory<StepSnapshot> stepHistory;

    public GuiController(GraphPanel gp, MatrixPanel mp, LogPanel lp) {
        this.graphPanel = gp;
        this.matrixPanel = mp;
        this.logPanel = lp;
        this.graph = new Graph();
    }

    
    public Graph getGraph() {
        return graph;
    }

    
    public String[] getVertexNames() {
        return vertexNames;
    }

    public void loadGraphFromFile(java.io.File file) throws java.io.IOException {
        Graph loaded = GraphFileLoader.loadFromFile(file);
        int n = loaded.size();

        graphPanel.clearGraph();
        this.graph = loaded;

        vertexNames = new String[n];
        for (int i = 0; i < n; i++) {
            Graph.Vertex v = loaded.getVertex(i);
            vertexNames[i] = v.id;
            graphPanel.addVertex((int) v.x, (int) v.y, v.id);
        }

        graphPanel.setVertexNames(vertexNames);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && loaded.matrix[i][j] < Graph.INF / 2) {
                    graphPanel.addEdge(i, j, (int) loaded.matrix[i][j]);
                }
            }
        }

        matrixPanel.renderMatrixPanel(loaded.matrix);
        graphPanel.repaint();
        logPanel.printLog("Граф загружен из файла: " + file.getName() + " (" + n + " вершин).");

        prepareStepMode();
    }

    public void prepareStepMode() {
        floydWarshall = new FloydWarshall(graph);
        floydWarshall.init();
        stepHistory = new StepHistory<>();
        stepHistory.push(captureSnapshot());
    }

    public boolean canStepForward() {
        if (stepHistory == null) {
            return false;
        }
        return stepHistory.canStepForward() || (floydWarshall != null && !floydWarshall.finished);
    }

    public boolean canStepBackward() {
        return stepHistory != null && stepHistory.canStepBackward();
    }

    public StepSnapshot getCurrentStep() {
        return stepHistory == null ? null : stepHistory.current();
    }

    public void stepForward() {
        if (stepHistory == null) {
            logPanel.printLog("Граф не загружен — шагать некуда.");
            return;
        }
        if (stepHistory.canStepForward()) {
            applySnapshot(stepHistory.stepForward());
            return;
        }
        if (floydWarshall == null || floydWarshall.finished) {
            logPanel.printLog("Алгоритм уже завершён — шагать дальше некуда.");
            return;
        }
        advanceOneStep();
        applySnapshot(stepHistory.current());
    }

    public void stepBackward() {
        if (stepHistory == null || !stepHistory.canStepBackward()) {
            logPanel.printLog("Шагать назад некуда.");
            return;
        }
        applySnapshot(stepHistory.stepBackward());
    }

    private void advanceOneStep() {
        int oldK = floydWarshall.currentK;
        int oldI = floydWarshall.currentI;
        int oldJ = floydWarshall.currentJ;
        double oldValue = floydWarshall.getDistMatrix()[oldI][oldJ];

        floydWarshall.step();
        stepHistory.push(captureSnapshot());

        StepSnapshot snap = stepHistory.current();
        String kName = vertexName(snap.k);
        String iName = vertexName(snap.i);
        String jName = vertexName(snap.j);

        boolean hadImprovement = false;
        String improvementMessage = "";
        for (FloydWarshall.EdgeUpdate u : snap.updates) {
            if (u.improved) {
                hadImprovement = true;
                double viaK = u.value;
                double dIK = floydWarshall.getDistMatrix()[u.from][snap.k];
                double dKJ = floydWarshall.getDistMatrix()[snap.k][u.to];
                String fromName = vertexName(u.from);
                String toName = vertexName(u.to);
                improvementMessage = "Найден более короткий путь из " + fromName + " в " + toName + 
                    ": " + String.format("%.0f", dIK) + " + " + String.format("%.0f", dKJ) + 
                    " = " + String.format("%.0f", viaK) + " < " + String.format("%.0f", oldValue);
                break;
            }
        }

        if (hadImprovement) {
            logPanel.printLog(improvementMessage);
        } else {
            double dIK = floydWarshall.getDistMatrix()[oldI][oldK];
            double dKJ = floydWarshall.getDistMatrix()[oldK][oldJ];
            double viaK = dIK + dKJ;
            String fromName = vertexName(oldI);
            String toName = vertexName(oldJ);
            String kNameForLog = vertexName(oldK);
            logPanel.printLog("Улучшение не требуется: D[" + fromName + "][" + kNameForLog + "] + D[" + kNameForLog + "][" + toName + 
                "] = " + String.format("%.0f", dIK) + " + " + String.format("%.0f", dKJ) + 
                " = " + String.format("%.0f", viaK) + " >= D[" + fromName + "][" + toName + 
                "] = " + String.format("%.0f", oldValue));
        }
    }

    private StepSnapshot captureSnapshot() {
        double[][] distCopy = deepCopy(floydWarshall.getDistMatrix());
        java.util.List<FloydWarshall.EdgeUpdate> updatesCopy = new java.util.ArrayList<>(floydWarshall.lastUpdates);
        return new StepSnapshot(
                floydWarshall.currentK, floydWarshall.currentI, floydWarshall.currentJ,
                distCopy, updatesCopy, floydWarshall.finished);
    }

    private static double[][] deepCopy(double[][] source) {
        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    private void applySnapshot(StepSnapshot snap) {
        matrixPanel.renderMatrixPanel(snap.dist);

        String kName = vertexName(snap.k);
        String iName = vertexName(snap.i);
        String jName = vertexName(snap.j);

        if (!snap.finished) {
            double dIK = snap.dist[snap.i][snap.k];
            double dKJ = snap.dist[snap.k][snap.j];
            double viaK = dIK + dKJ;
            double direct = snap.dist[snap.i][snap.j];

            String logMsg = "Итерация k=" + kName + ", i=" + iName + ", j=" + jName +
                ". Проверяем D[" + iName + "][" + kName + "] + D[" + kName + "][" + jName + 
                "] = " + String.format("%.0f", dIK) + " + " + String.format("%.0f", dKJ) + 
                " = " + String.format("%.0f", viaK) + " ? D[" + iName + "][" + jName + 
                "] = " + String.format("%.0f", direct);

            boolean hadImprovement = false;
            for (FloydWarshall.EdgeUpdate u : snap.updates) {
                if (u.improved) {
                    hadImprovement = true;
                    break;
                }
            }

            if (hadImprovement) {
                logPanel.printLog(logMsg + " — найден более короткий путь!");
            } else {
                logPanel.printLog(logMsg + " — улучшение не требуется");
            }
        } else {
            logPanel.printLog("Алгоритм завершён. Найдены все кратчайшие пути.");
        }

        if (snap.finished) {
            graphPanel.clearHighlights();
            matrixPanel.clearHighlight();
        } else {
            graphPanel.setHighlight(snap.k, snap.i, snap.j);
            graphPanel.setHighlightedEdges(snap.updates);
            matrixPanel.setCounters(snap.k, snap.i, snap.j);
            matrixPanel.highlightCells(snap.updates);
        }
    }

    private String vertexName(int index) {
        if (vertexNames != null && index >= 0 && index < vertexNames.length) {
            return vertexNames[index];
        }
        return String.valueOf(index + 1);
    }

    public static class StepSnapshot {
        public final int k, i, j;
        public final double[][] dist;
        public final java.util.List<FloydWarshall.EdgeUpdate> updates;
        public final boolean finished;

        public StepSnapshot(int k, int i, int j, double[][] dist,
                             java.util.List<FloydWarshall.EdgeUpdate> updates, boolean finished) {
            this.k = k;
            this.i = i;
            this.j = j;
            this.dist = dist;
            this.updates = updates;
            this.finished = finished;
        }
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

        graphPanel.setVertexNames(vertexNames);

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            graph.addVertex(vertexNames[i], x, y);
            graphPanel.addVertex(x, y, vertexNames[i]);
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

        prepareStepMode();
        while (!floydWarshall.finished) {
            advanceOneStep();
        }

        double[][] dist = floydWarshall.getDistMatrix();

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