import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolbarPanel extends JPanel {
    private final JButton downloadButton;    // загрузить
    private final JButton saveButton;        // сохранить
    private final JButton startButton;       // выполнить
    private final JButton backstepButton;    // назад
    private final JButton nextstepButton;    // вперёд
    private final JButton addVertexButton;   // +вершина
    private final JButton addEdgeButton;     // +ребро
    private final JButton infoButton;        // инфо

    public ToolbarPanel() {
        setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(98, 112, 177)));
        setBackground(new Color(17, 30, 94));

        downloadButton = new JButton("Загрузить");
        saveButton = new JButton("Сохранить");
        startButton = new JButton("Выполнить");
        backstepButton = new JButton("Назад");
        nextstepButton = new JButton("Вперёд");
        addVertexButton = new JButton("+вершина");
        addEdgeButton = new JButton("+ребро");
        infoButton = new JButton("Инфо");

        startButton.setForeground(new Color(59, 100, 220));
        infoButton.setForeground(new Color(100, 100, 100));

        add(downloadButton);
        add(saveButton);
        add(startButton);
        add(backstepButton);
        add(nextstepButton);
        add(addVertexButton);
        add(infoButton);
    }

    // сеттеры для слушателей кнопок
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

    // управление доступностью кнопок навигации
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