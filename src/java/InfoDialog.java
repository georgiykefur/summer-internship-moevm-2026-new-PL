
import javax.swing.*;
import java.awt.*;

public class InfoDialog extends JDialog {

    public InfoDialog(Frame parent) {
        super(parent, "Справка", true);
        setSize(550, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        content.add(makeSection("Вершины"));
        content.add(makeText("k — промежуточная вершина (красный)"));
        content.add(makeText("i — вершина-источник (синий)"));
        content.add(makeText("j — вершина-назначение (зелёный)"));

        content.add(Box.createVerticalStrut(16));
        content.add(makeSection("Рёбра"));
        content.add(makeText("Серый — обычное ребро"));
        content.add(makeText("Ярко-зелёный, толще — путь улучшен"));
        content.add(makeText("Зелёный пунктир — новый путь (раньше был ∞)"));

        content.add(Box.createVerticalStrut(16));
        content.add(makeSection("Матрица"));
        content.add(makeText("Жёлтая ячейка — значение обновлено на этом шаге"));

        content.add(Box.createVerticalStrut(16));
        content.add(makeSection("Шаг вперёд"));
        content.add(makeText("Один шаг = одна итерация внутреннего цикла."));
        content.add(makeText("Алгоритм проверяет: можно ли улучшить"));
        content.add(makeText("путь из i в j, проходя через k."));
        content.add(makeText("Всего итераций: n³  (n — число вершин)"));

        content.add(Box.createVerticalStrut(20));

        JButton closeBtn = new JButton("Понятно");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());
        content.add(closeBtn);

        add(new JScrollPane(content));
    }

    private JLabel makeSection(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel makeText(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 3, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
