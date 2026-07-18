import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class LogPanel extends JPanel {

    private JTextArea logArea;   // область для вывода лога

    public LogPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(17, 30, 94));

        // рамка
        Border lineBorder = BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(180, 195, 240));
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

        // заголовок
        JLabel titleLabel = new JLabel("Лог выполнения", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(new Color(17, 30, 94));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        // текстовая область
        logArea = new JTextArea();
        logArea.setBackground(Color.BLACK);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setForeground(Color.WHITE);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    // печать сообщения в лог
    public void printLog(String message) {
        if (message == null || message.trim().isEmpty()) {
            logArea.append(" \n");   // пробел = видимая пустая строка
        } else {
            logArea.append("> " + message + "\n");
        }
        // автопрокрутка вниз
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}