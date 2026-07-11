import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

/**
 * Demo-окно Харченко Я.К.
 *
 * Итерация 0: навигация "Шаг вперёд" / "Шаг назад" по стеку заглушечных
 * состояний (StepHistory); кнопка "Загрузить" была только элементом
 * интерфейса без логики.
 *
 * Итерация 1 (эта версия): кнопка "Загрузить" теперь реально открывает файл
 * графа, парсит его в модель Graph через GraphFileLoader
 * и показывает полученную матрицу весов (dist) в таблице. Навигация по шагам
 * по-прежнему работает на заглушечных состояниях — интеграция с настоящими
 * шагами алгоритма Флойда-Уоршелла от Гриценко в этой итерации не входит
 * в задачу Харченко.
 *
 * Окно не встраивается в макет Кузьмина и не связано с алгоритмом Гриценко —
 * полная интеграция частей команды здесь не производится.
 */
public class StepDemoWindow extends JFrame {

    private final StepHistory<String> history = new StepHistory<>();

    private JButton loadButton;
    private JButton forwardButton;
    private JButton backwardButton;
    private JTextArea stateArea;
    private JLabel graphStatusLabel;
    private JTable distTable;

    public StepDemoWindow() {
        super("Floyd–Warshall — демо шагов + загрузка графа (Харченко, итерация 1)");
        fillDemoStates();
        buildUi();
        refreshView();
    }

    /** Наполняет историю заглушечными состояниями для демонстрации шагов. */
    private void fillDemoStates() {
        for (int i = 0; i < 5; i++) {
            history.push(demoDescription(i));
        }
        // После push текущим стал последний шаг — для демонстрации логично
        // начать показ с самого начала истории.
        while (history.canStepBackward()) {
            history.stepBackward();
        }
    }

    private String demoDescription(int i) {
        return switch (i) {
            case 0 -> "начальное состояние (исходная матрица расстояний)";
            case 1 -> "k=0: рассмотрены пути через вершину 0";
            case 2 -> "k=1: рассмотрены пути через вершину 1";
            case 3 -> "k=2: рассмотрены пути через вершину 2";
            default -> "k=3: рассмотрены пути через вершину 3 (алгоритм завершён)";
        };
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildToolBar(), BorderLayout.NORTH);
        add(buildStatePanel(), BorderLayout.CENTER);
        add(buildGraphTablePanel(), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(640, 520));
        pack();
        setLocationRelativeTo(null);
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        loadButton = new JButton("Загрузить");
        loadButton.setToolTipText("Загрузить граф из файла (формат — см. data/sample_graph.txt)");
        loadButton.addActionListener(e -> onLoadClicked());

        forwardButton = new JButton("Шаг вперёд");
        forwardButton.addActionListener(e -> {
            history.stepForward();
            refreshView();
        });

        backwardButton = new JButton("Шаг назад");
        backwardButton.addActionListener(e -> {
            history.stepBackward();
            refreshView();
        });

        toolBar.add(loadButton);
        toolBar.addSeparator();
        toolBar.add(backwardButton);
        toolBar.add(forwardButton);
        return toolBar;
    }

    private JPanel buildStatePanel() {
        stateArea = new JTextArea();
        stateArea.setEditable(false);
        stateArea.setLineWrap(true);
        stateArea.setWrapStyleWord(true);
        stateArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        stateArea.setMargin(new java.awt.Insets(8, 8, 8, 8));

        JPanel statePanel = new JPanel(new BorderLayout());
        statePanel.setBorder(BorderFactory.createTitledBorder("Текущее состояние (демо-шаги)"));
        statePanel.add(new JScrollPane(stateArea), BorderLayout.CENTER);
        return statePanel;
    }

    private JPanel buildGraphTablePanel() {
        graphStatusLabel = new JLabel("Граф не загружен.");
        graphStatusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        distTable = new JTable(createNonEditableModel(new Object[0][0], new String[0]));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Матрица dist (загруженный граф)"));
        tablePanel.add(graphStatusLabel, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(distTable);
        tableScroll.setPreferredSize(new Dimension(600, 180));
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        return tablePanel;
    }

    /** Обновляет текст состояния и доступность кнопок навигации по границам истории. */
    private void refreshView() {
        int index = history.currentIndexValue();
        int total = history.size();
        stateArea.setText("Шаг " + index + " / " + (total - 1) + ": " + history.current());

        forwardButton.setEnabled(history.canStepForward());
        backwardButton.setEnabled(history.canStepBackward());
    }

    private void appendLog(String message) {
        stateArea.setText(stateArea.getText() + System.lineSeparator() + message);
    }

    /** Открывает диалог выбора файла, парсит граф и показывает матрицу dist в таблице. */
    private void onLoadClicked() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Загрузить граф из файла");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            Graph graph = GraphFileLoader.loadFromFile(file);
            showGraphInTable(file, graph);
            appendLog("Граф загружен: " + file.getName() + " (" + graph.vertexCount() + " вершин).");
        } catch (IOException | GraphParseException ex) {
            String message = "Не удалось загрузить граф из файла \"" + file.getName() + "\": " + ex.getMessage();
            appendLog(message);
            JOptionPane.showMessageDialog(this, message, "Ошибка загрузки графа", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Заполняет таблицу матрицей dist только что загруженного графа. */
    private void showGraphInTable(File file, Graph graph) {
        int n = graph.vertexCount();

        String[] columnNames = new String[n + 1];
        columnNames[0] = "i \\ j";
        for (int j = 0; j < n; j++) {
            columnNames[j + 1] = String.valueOf(j);
        }

        Object[][] rows = new Object[n][n + 1];
        for (int i = 0; i < n; i++) {
            rows[i][0] = String.valueOf(i);
            for (int j = 0; j < n; j++) {
                rows[i][j + 1] = formatWeight(graph.getDistance(i, j));
            }
        }

        distTable.setModel(createNonEditableModel(rows, columnNames));
        graphStatusLabel.setText("Загружен: " + file.getName() + " — вершин: " + n);
    }

    /** Таблица только для просмотра: модель, у которой все ячейки нередактируемы. */
    private static DefaultTableModel createNonEditableModel(Object[][] rows, String[] columnNames) {
        return new DefaultTableModel(rows, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static String formatWeight(double value) {
        if (Double.isInfinite(value)) {
            return "INF";
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StepDemoWindow window = new StepDemoWindow();
            window.setVisible(true);
        });
    }
}