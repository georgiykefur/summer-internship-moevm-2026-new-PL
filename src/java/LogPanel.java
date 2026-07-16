import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class LogPanel extends JPanel {

    private JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout());
        Border lineBorder = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY);
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

        JLabel titleLabel = new JLabel("Лог выполнения", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void printLog(String message) {
        logArea.append("> " + message + "\n");

        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}