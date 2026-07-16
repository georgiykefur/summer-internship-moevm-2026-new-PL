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

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, matrixPanel, logPanel);
        rightSplit.setResizeWeight(0.5);
        rightSplit.setDividerSize(5);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, rightSplit);
        mainSplit.setResizeWeight(1.0);
        mainSplit.setDividerSize(5);
        mainSplit.setDividerLocation(700);

        add(mainSplit, BorderLayout.CENTER);

        int INF = Integer.MAX_VALUE;
        int[][] adjacencyMatrix = {
                {0,   3, INF, 7},
                {8,   0,   2, INF},
                {5, INF,   0,   1},
                {2, INF, INF,   0}
        };

        guiController.initialGraph(adjacencyMatrix);
        guiController.prepareStepMode();

        // Передаём имена вершин в MatrixPanel
        String[] names = {"A", "B", "C", "D"};
        matrixPanel.setVertexNames(names);

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
                // используем геттер
                Graph loadedGraph = guiController.getGraph();
                String[] loadedNames = new String[loadedGraph.size()];
                for (int i = 0; i < loadedNames.length; i++) {
                    loadedNames[i] = loadedGraph.getVertex(i).id;
                }
                matrixPanel.setVertexNames(loadedNames);
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

        toolbarPanel.setBackstepButton(e -> {
            guiController.stepBackward();
            syncStepButtons();
        });

        toolbarPanel.setNextstepButton(e -> {
            guiController.stepForward();
            syncStepButtons();
        });

        toolbarPanel.setInfoButtonListener(e -> {
            InfoDialog dialog = new InfoDialog(this);
            dialog.setVisible(true);
        });

        syncStepButtons();
    }

    private void syncStepButtons() {
        toolbarPanel.setBackstepButtonEnabled(guiController.canStepBackward());
        toolbarPanel.setNextstepButtonEnabled(guiController.canStepForward());
    }
}