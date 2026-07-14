import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private ToolbarPanel toolbarPanel;
    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private GuiController guiController;

    public MainFrame() {
        super("Алгоритм Флойда-Уоршелла");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        toolbarPanel = new ToolbarPanel();
        graphPanel = new GraphPanel();
        matrixPanel = new MatrixPanel();
        logPanel = new LogPanel();

        guiController = new GuiController(graphPanel, matrixPanel, logPanel);

        setLayout(new BorderLayout());

        add(toolbarPanel, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1, 0, 0));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.add(matrixPanel);
        rightPanel.add(logPanel);
        add(rightPanel, BorderLayout.EAST);

        // INF = Integer.MAX_VALUE означает отсутствие ребра
        int INF = Integer.MAX_VALUE;
        int[][] adjacencyMatrix = {
            {0, 2, 10},
            {INF, 0, 3},
            {1, INF, 0}
        };

        guiController.initialGraph(adjacencyMatrix);
        guiController.prepareStepMode();

        toolbarPanel.setStartButtonListener(e -> {
            guiController.runAlgorithm();
            syncStepButtons();
        });

        toolbarPanel.setAddVertexButtonListener(e -> {
            graphPanel.addVertex();
            logPanel.printLog("Добавлена вершина " + graphPanel.getCount());
        });

        toolbarPanel.setAddEdgeButtonListener(e -> {
            logPanel.printLog("Кнопка +ребро нажата");
        });

        // Итерация 1 (Харченко): реальная загрузка графа из файла и парсинг в модель.
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
                syncStepButtons();
            } catch (IOException | GraphParseException ex) {
                String message = "Не удалось загрузить граф из файла \"" + file.getName() + "\": " + ex.getMessage();
                logPanel.printLog(message);
                JOptionPane.showMessageDialog(this, message, "Ошибка загрузки графа", JOptionPane.ERROR_MESSAGE);
            }
        });

        toolbarPanel.setSaveButton(e -> {
            logPanel.printLog("Сохранение в файл будет в итерации 2");
        });

        // Итерация 1 (Харченко): реальные шаги вперёд/назад по алгоритму.
        toolbarPanel.setBackstepButton(e -> {
            guiController.stepBackward();
            syncStepButtons();
        });

        toolbarPanel.setNextstepButton(e -> {
            guiController.stepForward();
            syncStepButtons();
        });

        syncStepButtons();
    }

    /** Блокирует "Назад"/"Вперёд" на границах истории шагов алгоритма. */
    private void syncStepButtons() {
        toolbarPanel.setBackstepButtonEnabled(guiController.canStepBackward());
        toolbarPanel.setNextstepButtonEnabled(guiController.canStepForward());
    }
}