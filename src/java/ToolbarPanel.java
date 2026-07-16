import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolbarPanel extends JPanel {
    private final JButton downloadButton;
    private final JButton saveButton;
    private final JButton startButton;
    private final JButton backstepButton;
    private final JButton nextstepButton;
    private final JButton addVertexButton;
    private final JButton addEdgeButton;
    private final JButton infoButton;

    public ToolbarPanel() {
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY));

        downloadButton = new JButton("Загрузить");
        saveButton = new JButton("Сохранить");
        startButton = new JButton("Запустить");
        backstepButton = new JButton("Назад");
        nextstepButton = new JButton("Вперед");
        addVertexButton = new JButton("+вершина");
        addEdgeButton = new JButton("+ребро");
        infoButton      = new JButton("Инфо");

        startButton.setForeground(Color.BLUE);
        infoButton.setForeground(new Color(100, 100, 100));

        add(downloadButton);
        add(saveButton);
        add(startButton);
        add(backstepButton);
        add(nextstepButton);
        add(addVertexButton);
//        add(addEdgeButton);
        add(infoButton);
    }

    public void setAddVertexButtonListener(ActionListener listener) {
        addVertexButton.addActionListener(listener);
    }

    public void setAddEdgeButtonListener(ActionListener listener) {
        addEdgeButton.addActionListener(listener);
    }

    public void setDownloadButton(ActionListener listener) {
        downloadButton.addActionListener(listener);
    }

    public void setSaveButton(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void setStartButtonListener(ActionListener listener) {
        startButton.addActionListener(listener);
    }

    public void setBackstepButton(ActionListener listener) {
        backstepButton.addActionListener(listener);
    }

    public void setNextstepButton(ActionListener listener) {
        nextstepButton.addActionListener(listener);
    }

    // Итерация 1 (Харченко): нужно для блокировки кнопок на границах истории шагов.
    public void setBackstepButtonEnabled(boolean enabled) {
        backstepButton.setEnabled(enabled);
    }

    public void setNextstepButtonEnabled(boolean enabled) {
        nextstepButton.setEnabled(enabled);
    }

    public void setInfoButtonListener(ActionListener listener) {
        infoButton.addActionListener(listener);
    }
}