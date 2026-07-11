import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Отдельное demo-окно Харченко Я.К. для итерации 0.
 *
 * Показывает единственную реально работающую часть его задачи: навигацию
 * "Шаг вперёд" / "Шаг назад" по стеку заглушечных состояний ({@link StepHistory}).
 * Кнопка "Загрузить" присутствует как элемент интерфейса — по клику лишь
 * выводит сообщение о том, что загрузка файла будет реализована в итерации 1
 * (полноценная загрузка и парсинг графа сознательно не входят в эту итерацию).
 *
 * Окно не встраивается в макет Кузьмина и не связано с алгоритмом Гриценко —
 * интеграция частей команды начнётся в итерации 1.
 */
public class StepDemoWindow extends JFrame {

    private final StepHistory<String> history = new StepHistory<>();

    private JButton loadButton;
    private JButton forwardButton;
    private JButton backwardButton;
    private JTextArea stateArea;

    public StepDemoWindow() {
        super("Floyd–Warshall — демо шагов (Харченко, итерация 0)");
        fillDemoStates();
        buildUi();
        refreshView();
    }

    /** Наполняет историю заглушечными состояниями для демонстрации. */
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

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        loadButton = new JButton("Загрузить");
        loadButton.setToolTipText("Полноценная загрузка и парсинг файла графа — итерация 1");
        loadButton.addActionListener(e ->
                appendLog("Загрузка файла будет реализована в итерации 1."));

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

        add(toolBar, BorderLayout.NORTH);

        stateArea = new JTextArea();
        stateArea.setEditable(false);
        stateArea.setLineWrap(true);
        stateArea.setWrapStyleWord(true);
        stateArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        stateArea.setMargin(new java.awt.Insets(8, 8, 8, 8));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Текущее состояние"));
        bottomPanel.add(new JScrollPane(stateArea), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(520, 260));
        pack();
        setLocationRelativeTo(null);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StepDemoWindow window = new StepDemoWindow();
            window.setVisible(true);
        });
    }
}
