public class GuiController {

    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private Graph graph;
    private FloydWarshall floydWarshall;
    private String[] vertexNames;

    // --- Итерация 1 (Харченко): пошаговый режим для кнопок "Назад"/"Вперёд" ---
    private StepHistory<StepSnapshot> stepHistory;

    public GuiController(GraphPanel gp, MatrixPanel mp, LogPanel lp) {
        this.graphPanel = gp;
        this.matrixPanel = mp;
        this.logPanel = lp;
        this.graph = new Graph();
    }

    /**
     * Загружает граф из файла (Харченко, итерация 1) и полностью заменяет
     * им текущий граф на холсте и в таблице. Координаты вершин берутся
     * прямо из файла (в отличие от {@link #initialGraph}, который всегда
     * раскладывает вершины по кругу).
     *
     * @throws java.io.IOException если файл не удалось прочитать
     * @throws GraphParseException если содержимое файла не по формату
     */
    public void loadGraphFromFile(java.io.File file) throws java.io.IOException {
        Graph loaded = GraphFileLoader.loadFromFile(file);
        int n = loaded.size();

        graphPanel.clearGraph();
        this.graph = loaded;

        vertexNames = new String[n];
        for (int i = 0; i < n; i++) {
            Graph.Vertex v = loaded.getVertex(i);
            vertexNames[i] = v.id;
            graphPanel.addVertex((int) v.x, (int) v.y);
        }
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

    /**
     * Готовит алгоритм к пошаговому проходу по текущему графу: создаёт
     * {@link FloydWarshall}, инициализирует его и кладёт в историю
     * начальный снимок (шаг "до старта"). Вызывается после загрузки графа
     * (из файла или дефолтной матрицы) и в начале {@link #runAlgorithm}.
     */
    public void prepareStepMode() {
        floydWarshall = new FloydWarshall(graph);
        floydWarshall.init();
        stepHistory = new StepHistory<>();
        stepHistory.push(captureSnapshot());
    }

    /** @return можно ли сейчас шагнуть вперёд (есть кэшированный шаг или алгоритм ещё не завершён) */
    public boolean canStepForward() {
        if (stepHistory == null) {
            return false;
        }
        return stepHistory.canStepForward() || (floydWarshall != null && !floydWarshall.finished);
    }

    /** @return можно ли сейчас шагнуть назад */
    public boolean canStepBackward() {
        return stepHistory != null && stepHistory.canStepBackward();
    }

    /** Текущий снимок шага — точка расширения для подсветки Кузьмина (см. applySnapshot). */
    public StepSnapshot getCurrentStep() {
        return stepHistory == null ? null : stepHistory.current();
    }

    /** Обработчик кнопки "Шаг вперёд". */
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

    /** Обработчик кнопки "Шаг назад". Сам алгоритм назад не умеет — используем историю снимков. */
    public void stepBackward() {
        if (stepHistory == null || !stepHistory.canStepBackward()) {
            logPanel.printLog("Шагать назад некуда.");
            return;
        }
        applySnapshot(stepHistory.stepBackward());
    }

    /** Выполняет один реальный шаг алгоритма и сохраняет его в историю. */
    private void advanceOneStep() {
        floydWarshall.step();
        stepHistory.push(captureSnapshot());
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

    /** Применяет снимок шага к таблице и логу; холст просто просят перерисоваться. */
    private void applySnapshot(StepSnapshot snap) {
        matrixPanel.renderMatrixPanel(snap.dist);

        StringBuilder message = new StringBuilder("Шаг: k=").append(vertexName(snap.k))
                .append(", i=").append(vertexName(snap.i))
                .append(", j=").append(vertexName(snap.j));
        for (FloydWarshall.EdgeUpdate u : snap.updates) {
            if (u.improved) {
                message.append(" — найден более короткий путь ")
                        .append(vertexName(u.from)).append("→").append(vertexName(u.to))
                        .append(" = ").append((int) u.value);
            }
        }
        if (snap.finished) {
            message.append(" — алгоритм завершён");
        }
        logPanel.printLog(message.toString());

        // TODO(Кузьмин): здесь точка входа для подсветки текущей вершины k и анимации
        // изменения цвета рёбер (см. ваш метод в GraphPanel) — данные уже готовы:
        // snap.k / snap.i / snap.j (индексы вершин) и snap.updates (какие рёбра/как изменились).
        graphPanel.repaint();
    }

    private String vertexName(int index) {
        return (vertexNames != null && index >= 0 && index < vertexNames.length) ? vertexNames[index] : String.valueOf(index);
    }

    /** Снимок состояния алгоритма на конкретном шаге — то, что хранится в {@link StepHistory}. */
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

        // Итерация 1 (Харченко): вместо floydWarshall.runFull() идём через тот же
        // пошаговый механизм, что и кнопки "Шаг вперёд"/"Шаг назад" — так после
        // "Запустить" по истории тоже можно пройтись назад до любого промежуточного шага.
        prepareStepMode();
        while (!floydWarshall.finished) {
            advanceOneStep();
        }

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