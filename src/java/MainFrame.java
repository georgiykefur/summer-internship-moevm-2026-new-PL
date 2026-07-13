// --- ToolbarPanel ---
// GraphPanel --- MatrixPanel
// GraphPanel --- LogPanel

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ToolbarPanel toolbarPanel;
    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;

    public MainFrame(){
        // Настройки окна
        super("Алгоритм Флойд-Уоршелл");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        // Инициализация объектов окна
        toolbarPanel = new ToolbarPanel();
        graphPanel = new GraphPanel();
        matrixPanel = new MatrixPanel();
        logPanel = new LogPanel();

        // Настройка лэяута
        setLayout(new BorderLayout());

        // Матрица и логи справа в одном окне JPanel
        add(toolbarPanel, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1, 0, 0));
        rightPanel.setPreferredSize(new Dimension(300, 0)); // фикс размер

        // Добавление в правую панель
        rightPanel.add(matrixPanel);
        rightPanel.add(logPanel);

        // Добавление правой панели
        add(rightPanel, BorderLayout.EAST);

        toolbarPanel.setAddVertexButtonListener(e -> {
            logPanel.printLog("Кнопка +вершина нажата.");
        });

    }
}
