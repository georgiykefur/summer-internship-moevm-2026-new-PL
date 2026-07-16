import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
        graphPanel.setController(guiController);

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
//        String[] names = {"A", "B", "C", "D"};
//        matrixPanel.setVertexNames(names);

        toolbarPanel.setStartButtonListener(e -> {
            guiController.runAlgorithm();
            syncStepButtons();
        });

        toolbarPanel.setAddVertexButtonListener(e -> {
            // Определяем центр видимой области GraphPanel
            int cx = graphPanel.getWidth() / 2;
            int cy = graphPanel.getHeight() / 2;
            guiController.addVertex(cx, cy);
            syncStepButtons();
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
        setupZoomButtons();

    }

    private void setupZoomButtons() {
        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            public boolean contains(int x, int y) {
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

        JPanel zoomButtons = new JPanel();
        zoomButtons.setLayout(new BoxLayout(zoomButtons, BoxLayout.Y_AXIS));
        zoomButtons.setOpaque(false);
        zoomButtons.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 0));

        JButton zoomIn  = makeZoomButton("+");
        JButton zoomOut = makeZoomButton("−");

        zoomIn.addActionListener(e  -> graphPanel.applyZoomStep(1.4));
        zoomOut.addActionListener(e -> graphPanel.applyZoomStep(1.0 / 1.4));

        zoomButtons.add(zoomIn);
        zoomButtons.add(Box.createVerticalStrut(6));
        zoomButtons.add(zoomOut);

        glass.add(zoomButtons, BorderLayout.SOUTH);

        setGlassPane(glass);
        glass.setVisible(true);
    }



    private Component findComponentAt(Container container, int x, int y) {
        Component comp = container.findComponentAt(x, y);
        if (comp instanceof JButton) return null;
        return comp;
    }

    private JButton makeZoomButton(String text){
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(40, 40));
        button.setMaximumSize(new Dimension(40, 40));
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return button;
    }

    private void syncStepButtons() {
        toolbarPanel.setBackstepButtonEnabled(guiController.canStepBackward());
        toolbarPanel.setNextstepButtonEnabled(guiController.canStepForward());
    }
}