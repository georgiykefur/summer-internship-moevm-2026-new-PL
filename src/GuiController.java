import java.util.Arrays;

public class GuiController {

    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private Graph graph;
    private FloydWarshall floydWarshall;
    private String[] vertexNames;

    private StepHistory<StepSnapshot> stepHistory;
    private boolean algorithmStarted = false;

    // для логирования с отступами
    private int lastLoggedK = -1;
    private int lastLoggedI = -1;

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
        matrixPanel.setVertexNames(vertexNames);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && loaded.matrix[i][j] < Graph.INF / 2) {
                    graphPanel.addEdge(i, j, (int) loaded.matrix[i][j]);
                }
            }
        }

        matrixPanel.renderMatrixPanel(loaded.matrix);
        graphPanel.repaint();
        logPanel.printLog("граф загружен из файла: " + file.getName() + " (" + n + " вершин).");

        prepareStepMode();
    }

    public void prepareStepMode() {
        algorithmStarted = false;
        floydWarshall = new FloydWarshall(graph);
        floydWarshall.init();
        stepHistory = new StepHistory<>();
        stepHistory.push(captureSnapshot());
        lastLoggedK = -1;
        lastLoggedI = -1;
        graphPanel.clearPath();
        graphPanel.clearHighlights();
        matrixPanel.clearHighlight();
    }

    // генерация имени для новой вершины
    private String generateNextVertexName() {
        if (vertexNames == null || vertexNames.length == 0) {
            return "A";
        }
        
        char maxChar = 'A' - 1;
        for (String name : vertexNames) {
            if (name != null && name.length() == 1) {
                char c = name.charAt(0);
                if (c >= 'A' && c <= 'Z' && c > maxChar) {
                    maxChar = c;
                }
            }
        }
        
        if (maxChar >= 'A' && maxChar < 'Z') {
            return String.valueOf((char) (maxChar + 1));
        }
        
        int maxNum = 0;
        for (String name : vertexNames) {
            if (name != null && name.startsWith("V")) {
                try {
                    int num = Integer.parseInt(name.substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        
        return "V" + (maxNum + 1);
    }

    public void addVertex(int x, int y) {
        String newId = generateNextVertexName();

        graph.addVertex(newId, x, y);

        String[] newNames = Arrays.copyOf(vertexNames, vertexNames.length + 1);
        newNames[vertexNames.length] = newId;
        vertexNames = newNames;

        graphPanel.addVertex(x, y, newId);
        graphPanel.setVertexNames(vertexNames);
        matrixPanel.setVertexNames(vertexNames);

        matrixPanel.renderMatrixPanel(graph.matrix);

        prepareStepMode();

        logPanel.printLog("добавлена вершина " + newId + " (координаты " + x + ", " + y + ")");
    }

    public void addEdge(int fromIndex, int toIndex, int weight) {
        if (fromIndex < 0 || fromIndex >= vertexNames.length ||
            toIndex < 0 || toIndex >= vertexNames.length) {
            logPanel.printLog("ошибка: неверные индексы вершин");
            return;
        }

        String fromId = vertexNames[fromIndex];
        String toId = vertexNames[toIndex];

        graph.addEdge(fromId, toId, weight);
        graphPanel.addEdge(fromIndex, toIndex, weight);
        matrixPanel.renderMatrixPanel(graph.matrix);

        logPanel.printLog("добавлено ребро " + fromId + " → " + toId + " (вес " + weight + ")");

        prepareStepMode();
    }

    public void removeEdge(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= vertexNames.length ||
            toIndex < 0 || toIndex >= vertexNames.length) {
            logPanel.printLog("ошибка: неверные индексы вершин");
            return;
        }

        String fromId = vertexNames[fromIndex];
        String toId = vertexNames[toIndex];

        graph.addEdge(fromId, toId, Graph.INF);
        graphPanel.removeEdge(fromIndex, toIndex);
        matrixPanel.renderMatrixPanel(graph.matrix);

        logPanel.printLog("удалено ребро " + fromId + " → " + toId);

        prepareStepMode();
    }

    public void removeVertex(int index) {
        if (index < 0 || index >= vertexNames.length) return;

        String removedName = vertexNames[index];

        graph.removeVertex(index);

        String[] newNames = new String[vertexNames.length - 1];
        int pos = 0;
        for (int i = 0; i < vertexNames.length; i++) {
            if (i != index) {
                newNames[pos++] = vertexNames[i];
            }
        }
        vertexNames = newNames;

        graphPanel.removeVertex(index);

        graphPanel.setVertexNames(vertexNames);
        matrixPanel.setVertexNames(vertexNames);

        matrixPanel.renderMatrixPanel(graph.matrix);

        prepareStepMode();

        logPanel.printLog("вершина " + removedName + " удалена.");
    }

    public void updateEdgeWeight(int fromIndex, int toIndex, int newWeight) {
        if (fromIndex < 0 || fromIndex >= vertexNames.length ||
            toIndex < 0 || toIndex >= vertexNames.length) {
            logPanel.printLog("ошибка: неверные индексы вершин");
            return;
        }

        String fromId = vertexNames[fromIndex];
        String toId = vertexNames[toIndex];

        graph.addEdge(fromId, toId, newWeight);
        graphPanel.updateEdgeWeight(fromIndex, toIndex, newWeight);
        matrixPanel.renderMatrixPanel(graph.matrix);

        logPanel.printLog("вес ребра " + fromId + " → " + toId + " изменён на " + newWeight);

        prepareStepMode();
    }

    // синхронизация координат из graphpanel в graph
    public void syncCoordinatesFromPanel() {
        for (int i = 0; i < graph.size() && i < graphPanel.getVertexCount(); i++) {
            Graph.Vertex v = graph.getVertex(i);
            int x = graphPanel.getVertexX(i);
            int y = graphPanel.getVertexY(i);
            if (x != -1 && y != -1) {
                v.x = x;
                v.y = y;
            }
        }
    }

    public void saveGraphToFile(java.io.File file) throws java.io.IOException {
        syncCoordinatesFromPanel();
        GraphFileWriter.saveToFile(graph, file);
        logPanel.printLog("граф сохранён в файл: " + file.getName());
    }

    public void showPathForCell(int from, int to) {
        if (floydWarshall == null) {
            logPanel.printLog("алгоритм не инициализирован. запустите алгоритм сначала.");
            return;
        }
        if (!floydWarshall.finished) {
            logPanel.printLog("алгоритм ещё не завершён. дождитесь завершения.");
            return;
        }

        if (from < 0 || from >= vertexNames.length || to < 0 || to >= vertexNames.length) {
            logPanel.printLog("ошибка: неверные индексы вершин: from=" + from + ", to=" + to);
            return;
        }

        String fromName = vertexNames[from];
        String toName = vertexNames[to];

        graphPanel.clearPath();

        if (from == to) {
            logPanel.printLog("путь из " + fromName + " в " + toName + " — это сама вершина");
            return;
        }

        double[][] dist = floydWarshall.getDistMatrix();
        if (dist[from][to] >= Graph.INF / 2) {
            logPanel.printLog("путь из " + fromName + " в " + toName + " не существует (∞)");
            return;
        }

        java.util.List<Integer> pathIndices = floydWarshall.getPathIndices(from, to);

        if (pathIndices == null || pathIndices.isEmpty()) {
            logPanel.printLog("путь из " + fromName + " в " + toName + " не найден");
            return;
        }

        java.util.List<int[]> pathEdges = floydWarshall.getPathEdges(from, to);
        graphPanel.showPath(pathIndices, pathEdges);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathIndices.size(); i++) {
            if (i > 0) sb.append(" → ");
            int idx = pathIndices.get(i);
            if (idx >= 0 && idx < vertexNames.length) {
                sb.append(vertexNames[idx]);
            } else {
                sb.append("?");
            }
        }
        logPanel.printLog("путь " + fromName + " → " + toName + ": " + sb.toString() +
            " (длина = " + String.format("%.0f", dist[from][to]) + ")");
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
        algorithmStarted = true;
        graphPanel.clearPath();
        if (stepHistory == null) {
            logPanel.printLog("граф не загружен — шагать некуда.");
            return;
        }
        if (stepHistory.canStepForward()) {
            applySnapshot(stepHistory.stepForward());
            return;
        }
        if (floydWarshall == null || floydWarshall.finished) {
            logPanel.printLog("алгоритм уже завершён — шагать дальше некуда.");
            return;
        }
        advanceOneStep();
        applySnapshot(stepHistory.current());
    }

    public void stepBackward() {
        graphPanel.clearPath();
        if (stepHistory == null || !stepHistory.canStepBackward()) {
            logPanel.printLog("шагать назад некуда.");
            algorithmStarted = false;
            return;
        }
        applySnapshot(stepHistory.stepBackward());
        StepSnapshot snap = stepHistory.current();
        lastLoggedK = snap.k;
        lastLoggedI = snap.i;
    }

    // выполняет один шаг алгоритма с логированием
    private void advanceOneStep() {
        int oldK = floydWarshall.currentK;
        int oldI = floydWarshall.currentI;
        int oldJ = floydWarshall.currentJ;
        double oldValue = floydWarshall.getDistMatrix()[oldI][oldJ];

        // логирование с отступами
        if (oldK != lastLoggedK) {
            if (lastLoggedK != -1) {
                logPanel.printLog(" ");
            }
            String kName = vertexName(oldK);
            logPanel.printLog("    " + kName + ":");
            lastLoggedK = oldK;
            lastLoggedI = -1;
        }

        if (oldI != lastLoggedI) {
            String iName = vertexName(oldI);
            logPanel.printLog("        " + iName + ":");
            lastLoggedI = oldI;
        }

        floydWarshall.step();
        stepHistory.push(captureSnapshot());

        StepSnapshot snap = stepHistory.current();

        boolean hadImprovement = false;
        for (FloydWarshall.EdgeUpdate u : snap.updates) {
            if (u.improved) {
                hadImprovement = true;
                double viaK = u.value;
                double dIK = floydWarshall.getDistMatrix()[u.from][snap.k];
                double dKJ = floydWarshall.getDistMatrix()[snap.k][u.to];
                String fromName = vertexName(u.from);
                String toName = vertexName(u.to);
                logPanel.printLog("            " + fromName + "->" + toName + ": " +
                    String.format("%.0f", dIK) + " + " + String.format("%.0f", dKJ) +
                    " = " + String.format("%.0f", viaK) + " < " + String.format("%.0f", oldValue) +
                    " (улучшение)");
                break;
            }
        }

        if (!hadImprovement) {
            double dIK = floydWarshall.getDistMatrix()[oldI][oldK];
            double dKJ = floydWarshall.getDistMatrix()[oldK][oldJ];
            double viaK = dIK + dKJ;
            String fromName = vertexName(oldI);
            String toName = vertexName(oldJ);
            String kNameForLog = vertexName(oldK);
            logPanel.printLog("            " + fromName + "->" + toName + ": " +
                String.format("%.0f", dIK) + " + " + String.format("%.0f", dKJ) +
                " = " + String.format("%.0f", viaK) + " >= " + String.format("%.0f", oldValue) +
                " (без изменений)");
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
        matrixPanel.setVertexNames(vertexNames);

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
        logPanel.printLog("граф загружен. вершины: " + String.join(", ", vertexNames));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] != Integer.MAX_VALUE && i != j) {
                    logPanel.printLog("  " + vertexNames[i] + " - " + vertexNames[j] +
                        " (вес " + matrix[i][j] + ")");
                }
            }
        }

        lastLoggedK = -1;
        lastLoggedI = -1;
    }

    public boolean isAlgorithmRunning() {
        boolean result = algorithmStarted &&
                stepHistory != null &&
                stepHistory.current() != null &&
                !stepHistory.current().finished;
        return result;
    }

    public void runAlgorithm() {
        algorithmStarted = true;
        if (graph == null || graph.size() == 0) {
            logPanel.printLog("ошибка: граф не загружен");
            return;
        }

        graphPanel.clearHighlights();
        graphPanel.clearPath();
        matrixPanel.clearHighlight();

        logPanel.printLog("запуск алгоритма флойда-уоршелла...");
        logPanel.printLog(" ");

        prepareStepMode();
        lastLoggedK = -1;
        lastLoggedI = -1;

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
        System.out.println("  отрицательный цикл: " + (floydWarshall.hasNegativeCycle() ? "есть" : "нет"));

        logPanel.printLog(" ");
        logPanel.printLog("алгоритм завершён!");
        logPanel.printLog("матрица кратчайших расстояний:");
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

        logPanel.printLog("кратчайшие пути:");
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

        logPanel.printLog("отрицательный цикл: " + (floydWarshall.hasNegativeCycle() ? "есть" : "нет"));

        matrixPanel.renderMatrixPanel(dist);
        logPanel.printLog("матрица обновлена");

        lastLoggedK = -1;
        lastLoggedI = -1;
    }
}