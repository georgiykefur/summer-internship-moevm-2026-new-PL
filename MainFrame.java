import javax.swing.*;
import java.awt.*;

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

        toolbarPanel.setStartButtonListener(e -> guiController.runAlgorithm());

        toolbarPanel.setAddVertexButtonListener(e -> {
            graphPanel.addVertex();
            logPanel.printLog("Добавлена вершина " + graphPanel.getCount());
        });

        toolbarPanel.setAddEdgeButtonListener(e -> {
            logPanel.printLog("Кнопка +ребро нажата");
        });

        toolbarPanel.setDownloadButton(e -> {
            logPanel.printLog("Загрузка из файла будет в итерации 1");
        });

        toolbarPanel.setSaveButton(e -> {
            logPanel.printLog("Сохранение в файл будет в итерации 1");
        });

        toolbarPanel.setBackstepButton(e -> {
            logPanel.printLog("Шаг назад будет в итерации 1");
        });

        toolbarPanel.setNextstepButton(e -> {
            logPanel.printLog("Шаг вперёд будет в итерации 1");
        });
    }
}