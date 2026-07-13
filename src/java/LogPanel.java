import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {

    private JTextArea logArea;

    public LogPanel(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,1,0,0, Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel("Лог выполнения", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15,0,5,0));
        add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void printLog(String message){
        logArea.append("> " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
