import java.util.Arrays;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

// главный контроллер, связывает модель, алгоритм и интерфейс
public class GuiController {

    // ссылки на компоненты интерфейса
    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    // модель графа
    private Graph graph;
    // экземпляр алгоритма
    private FloydWarshall floydWarshall;
    // имена вершин
    private String[] vertexNames;

    // история шагов для кнопок "вперёд" и "назад"
    private StepHistory<StepSnapshot> stepHistory;
    // флаг, что алгоритм был запущен
    private boolean algorithmStarted = false;

    // для логирования с отступами
    private int lastLoggedK = -1;
    private int lastLoggedI = -1;

    // полоса прогресса
    private JProgressBar progressBar;
    // общее количество шагов алгоритма
    private int totalSteps = 0;
    // максимальный достигнутый индекс в истории
    private int maxStepInHistory = 0;

    public GuiController(GraphPanel gp, MatrixPanel mp, LogPanel lp) {
        this.graphPanel = gp;
        this.matrixPanel = mp;
        this.logPanel = lp;
        this.graph = new Graph();
    }

    // геттер для графа
    public Graph getGraph() {
        return graph;
    }

    // геттер для имён вершин
    public String[] getVertexNames() {
        return vertexNames;
    }

    // установка полосы прогресса
    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    // обновляет прогресс по текущему индексу в истории
    private void updateProgress() {
        if (progressBar == null || stepHistory == null) return;
        int currentIndex = stepHistory.currentIndexValue();

        // запоминаем максимальный индекс
        if (currentIndex > maxStepInHistory) {
            maxStepInHistory = currentIndex;
        }

        // максимум = общее количество шагов или максимальный индекс
        final int max = Math.max(totalSteps, maxStepInHistory);
        final int current = Math.min(currentIndex, max);

        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(max);
            progressBar.setValue(current);
            int percent = (int) ((current * 100.0) / max);
            if (current >= max) {
                progressBar.setString("100% — готово!");
            } else {
                progressBar.setString(percent + "%");
            }
        });
    }

    // сбрасывает прогресс при загрузке нового графа
    private void resetProgress() {
        if (progressBar == null) return;
        int n = graph.size();
        // алгоритм пропускает диагональные элементы, поэтому шагов n*n*(n-1)
        totalSteps = n * n * (n - 1);
        if (totalSteps <= 0) totalSteps = 1;
        maxStepInHistory = 0;
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(totalSteps);
            progressBar.setValue(0);
            progressBar.setString("0%");
        });
    }

    // устанавливает прогресс на 100%
    private void setProgressFinished() {
        if (progressBar == null) return;
        if (stepHistory != null) {
            maxStepInHistory = Math.max(maxStepInHistory, stepHistory.size() - 1);
        }
        final int max = Math.max(totalSteps, maxStepInHistory);
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(max);
            progressBar.setValue(max);
            progressBar.setString("100% — готово!");
        });
    }

    // загрузка графа из файла
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

    // подготавливает алгоритм к пошаговому выполнению
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

        int n = graph.size();
        totalSteps = n * n * (n - 1);
        if (totalSteps <= 0) totalSteps = 1;
        maxStepInHistory = 0;
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setMaximum(totalSteps);
                progressBar.setValue(0);
                progressBar.setString("0%");
            });
        }
    }

    // генерация имени для новой вершины
    private String generateNextVertexName() {
        if (vertexNames == null || vertexNames.length == 0) {
            return "A";
        }

        // ищем максимальную букву среди существующих
        char maxChar = 'A' - 1;
        for (String name : vertexNames) {
            if (name != null && name.length() == 1) {
                char c = name.charAt(0);
                if (c >= 'A' && c <= 'Z' && c > maxChar) {
                    maxChar = c;
                }
            }
        }

        // если есть буква меньше z, возвращаем следующую
        if (maxChar >= 'A' && maxChar < 'Z') {
            return String.valueOf((char) (maxChar + 1));
        }

        // иначе используем формат v1, v2, ...
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

    // добавление вершины
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

    // добавление ребра
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

    // удаление ребра
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

    // удаление вершины
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

    // обновление веса ребра
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

    // синхронизация координат из холста в модель перед сохранением
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

    // сохранение графа в файл
    public void saveGraphToFile(java.io.File file) throws java.io.IOException {
        syncCoordinatesFromPanel();
        GraphFileWriter.saveToFile(graph, file);
        logPanel.printLog("граф сохранён в файл: " + file.getName());
    }

    // показывает путь на холсте при клике на ячейку матрицы
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

    // можно ли шагнуть вперёд
    public boolean canStepForward() {
        if (stepHistory == null) {
            return false;
        }
        if (stepHistory.canStepForward()) {
            return true;
        }
        if (floydWarshall != null && !floydWarshall.finished) {
            return true;
        }
        return false;
    }

    // можно ли шагнуть назад
    public boolean canStepBackward() {
        return stepHistory != null && stepHistory.canStepBackward();
    }

    // получить текущий снимок состояния
    public StepSnapshot getCurrentStep() {
        return stepHistory == null ? null : stepHistory.current();
    }

    // шаг вперёд
    public void stepForward() {
        algorithmStarted = true;
        graphPanel.clearPath();
        if (stepHistory == null) {
            logPanel.printLog("граф не загружен — шагать некуда.");
            return;
        }
        if (stepHistory.canStepForward()) {
            applySnapshot(stepHistory.stepForward());
            updateProgress();
            return;
        }
        if (floydWarshall == null || floydWarshall.finished) {
            logPanel.printLog("алгоритм уже завершён — шагать дальше некуда.");
            setProgressFinished();
            return;
        }
        advanceOneStep();
        applySnapshot(stepHistory.current());
        updateProgress();

        if (floydWarshall.finished) {
            setProgressFinished();
        }
    }

    // шаг назад
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
        updateProgress();
    }

    // выполняет один шаг алгоритма и логирует его
    private void advanceOneStep() {
        int oldK = floydWarshall.currentK;
        int oldI = floydWarshall.currentI;
        int oldJ = floydWarshall.currentJ;
        double oldValue = floydWarshall.getDistMatrix()[oldI][oldJ];

        // начало нового цикла k — пустая строка и отступ
        if (oldK != lastLoggedK) {
            if (lastLoggedK != -1) {
                logPanel.printLog(" ");
            }
            String kName = vertexName(oldK);
            logPanel.printLog("    " + kName + ":");
            lastLoggedK = oldK;
            lastLoggedI = -1;
        }

        // начало нового цикла i — отступ
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

    // создаёт снимок текущего состояния алгоритма
    private StepSnapshot captureSnapshot() {
        double[][] distCopy = deepCopy(floydWarshall.getDistMatrix());
        java.util.List<FloydWarshall.EdgeUpdate> updatesCopy = new java.util.ArrayList<>(floydWarshall.lastUpdates);
        return new StepSnapshot(
                floydWarshall.currentK, floydWarshall.currentI, floydWarshall.currentJ,
                distCopy, updatesCopy, floydWarshall.finished);
    }

    // создаёт глубокую копию матрицы
    private static double[][] deepCopy(double[][] source) {
        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    // применяет снимок состояния (обновляет матрицу и подсветку)
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

    // возвращает имя вершины по индексу
    private String vertexName(int index) {
        if (vertexNames != null && index >= 0 && index < vertexNames.length) {
            return vertexNames[index];
        }
        return String.valueOf(index + 1);
    }

    // снимок состояния алгоритма на конкретном шаге
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

    // инициализация графа из матрицы (вершины по кругу)
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

    // проверка, выполняется ли алгоритм в данный момент
    public boolean isAlgorithmRunning() {
        boolean result = algorithmStarted &&
                stepHistory != null &&
                stepHistory.current() != null &&
                !stepHistory.current().finished;
        return result;
    }

    // полный запуск алгоритма в отдельном потоке
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

        // отдельный поток, чтобы не блокировать интерфейс
        new Thread(() -> {
            prepareStepMode();
            lastLoggedK = -1;
            lastLoggedI = -1;

            while (!floydWarshall.finished) {
                advanceOneStep();
                SwingUtilities.invokeLater(this::updateProgress);
            }

            SwingUtilities.invokeLater(() -> {
                setProgressFinished();

                double[][] dist = floydWarshall.getDistMatrix();

                // вывод в консоль
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

                // вывод в лог
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
            });
        }).start();
    }
}
