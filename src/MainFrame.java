import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

// главное окно приложения, собирает все панели
public class MainFrame extends JFrame {
    // панель с кнопками
    private ToolbarPanel toolbarPanel;
    // холст для графа
    private GraphPanel graphPanel;
    // таблица матрицы
    private MatrixPanel matrixPanel;
    // лог выполнения
    private LogPanel logPanel;
    // контроллер
    private GuiController guiController;
    // полоса прогресса
    private JProgressBar progressBar;

    public MainFrame() {
        super("Алгоритм Флойда-Уоршелла");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        // создаём компоненты
        toolbarPanel = new ToolbarPanel();
        graphPanel = new GraphPanel();
        matrixPanel = new MatrixPanel();
        logPanel = new LogPanel();

        // связываем всё через контроллер
        guiController = new GuiController(graphPanel, matrixPanel, logPanel);
        graphPanel.setController(guiController);

        setLayout(new BorderLayout());

        // создаём полосу прогресса
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(59, 100, 220));
        progressBar.setBackground(new Color(50, 50, 60));
        progressBar.setString("готов");
        guiController.setProgressBar(progressBar);

        // панель с тулбаром и прогрессом
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(toolbarPanel, BorderLayout.NORTH);
        northPanel.add(progressBar, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // вертикальный сплит для матрицы и лога
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, matrixPanel, logPanel);
        rightSplit.setResizeWeight(0.5);
        rightSplit.setDividerSize(5);

        // горизонтальный сплит для холста и правой панели
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, rightSplit);
        mainSplit.setResizeWeight(1.0);
        mainSplit.setDividerSize(5);
        mainSplit.setDividerLocation(700);

        // оформление границ
        rightSplit.setBorder(BorderFactory.createEmptyBorder());
        rightSplit.setBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(180, 195, 240)));
        mainSplit.setBorder(BorderFactory.createEmptyBorder());

        add(mainSplit, BorderLayout.CENTER);

        // пустой граф при старте
        int[][] adjacencyMatrix = {};
        guiController.initialGraph(adjacencyMatrix);
        guiController.prepareStepMode();

        // клик по ячейке матрицы → показ пути на холсте
        matrixPanel.setCellClickListener((from, to) -> {
            guiController.showPathForCell(from, to);
        });

        // кнопка "выполнить" — запуск алгоритма
        toolbarPanel.setStartButtonListener(e -> {
            guiController.runAlgorithm();
            syncStepButtons();
        });

        // кнопка "+вершина" — добавляет вершину в центр холста
        toolbarPanel.setAddVertexButtonListener(e -> {
            int cx = graphPanel.getWidth() / 2;
            int cy = graphPanel.getHeight() / 2;
            guiController.addVertex(cx, cy);
            syncStepButtons();
        });

        // кнопка "+ребро" — напоминание о контекстном меню
        toolbarPanel.setAddEdgeButtonListener(e -> {
            logPanel.printLog("используйте контекстное меню для добавления ребра");
        });

        // кнопка "загрузить" — выбор файла и загрузка графа
        toolbarPanel.setDownloadButton(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Загрузить граф из файла");
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            try {
                guiController.loadGraphFromFile(file);
                Graph loadedGraph = guiController.getGraph();
                String[] loadedNames = new String[loadedGraph.size()];
                for (int i = 0; i < loadedNames.length; i++) {
                    loadedNames[i] = loadedGraph.getVertex(i).id;
                }
                matrixPanel.setVertexNames(loadedNames);
                syncStepButtons();
            } catch (IOException | GraphParseException ex) {
                String message = "не удалось загрузить граф из файла \"" + file.getName() + "\": " + ex.getMessage();
                logPanel.printLog(message);
                JOptionPane.showMessageDialog(this, message, "Ошибка загрузки графа", JOptionPane.ERROR_MESSAGE);
            }
        });

        // кнопка "сохранить" — сохранение графа в файл
        toolbarPanel.setSaveButton(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Сохранить граф в файл");
            chooser.setSelectedFile(new File("graph.txt"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            try {
                guiController.saveGraphToFile(file);
                logPanel.printLog("граф сохранён в файл: " + file.getName());
            } catch (IOException ex) {
                String message = "не удалось сохранить граф в файл \"" + file.getName() + "\": " + ex.getMessage();
                logPanel.printLog(message);
                JOptionPane.showMessageDialog(this, message, "Ошибка сохранения графа", JOptionPane.ERROR_MESSAGE);
            }
        });

        // кнопка "назад" — шаг назад в истории
        toolbarPanel.setBackstepButton(e -> {
            guiController.stepBackward();
            syncStepButtons();
        });

        // кнопка "вперёд" — шаг вперёд в истории
        toolbarPanel.setNextstepButton(e -> {
            guiController.stepForward();
            syncStepButtons();
        });

        // кнопка "инфо" — окно справки
        toolbarPanel.setInfoButtonListener(e -> {
            InfoDialog dialog = new InfoDialog(this);
            dialog.setVisible(true);
        });

        // синхронизация состояния кнопок
        syncStepButtons();
        setupZoomButtons();
    }

    // кнопки зума на стеклянной панели
    private void setupZoomButtons() {
        // стеклянная панель для кнопок поверх холста
        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            public boolean contains(int x, int y) {
                // клики проходят сквозь стеклянную панель, кроме кнопок
                for (Component child : getComponents()) {
                    Point p = SwingUtilities.convertPoint(this, x, y, child);
                    if (child.contains(p.x, p.y)) {
                        return true;
                    }
                }
                return false;
            }
        };
        glass.setOpaque(false);

        // панель с кнопками зума
        JPanel zoomButtons = new JPanel();
        zoomButtons.setLayout(new BoxLayout(zoomButtons, BoxLayout.Y_AXIS));
        zoomButtons.setOpaque(false);
        zoomButtons.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 0));

        JButton zoomIn = makeZoomButton("+");
        JButton zoomOut = makeZoomButton("−");

        zoomIn.addActionListener(e -> graphPanel.applyZoomStep(1.4));
        zoomOut.addActionListener(e -> graphPanel.applyZoomStep(1.0 / 1.4));

        zoomButtons.add(zoomIn);
        zoomButtons.add(Box.createVerticalStrut(6));
        zoomButtons.add(zoomOut);

        glass.add(zoomButtons, BorderLayout.SOUTH);

        setGlassPane(glass);
        glass.setVisible(true);
    }

    // создание кнопки для зума
    private JButton makeZoomButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(40, 40));
        button.setMaximumSize(new Dimension(40, 40));
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return button;
    }

    // обновляет доступность кнопок навигации
    private void syncStepButtons() {
        toolbarPanel.setBackstepButtonEnabled(guiController.canStepBackward());
        toolbarPanel.setNextstepButtonEnabled(guiController.canStepForward());
    }
}
