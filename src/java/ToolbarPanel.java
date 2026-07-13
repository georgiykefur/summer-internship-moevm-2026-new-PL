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

    public ToolbarPanel(){
//        setLayout(new BorderLayout());
//        setBackground(new Color(213, 212, 212));
        setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.LIGHT_GRAY));

        downloadButton = new JButton("Загрузить");
        saveButton = new JButton("Сохранить");
        startButton = new JButton("Запустить");
        backstepButton = new JButton("Назад");
        nextstepButton = new JButton("Вперед");
        addVertexButton = new JButton("+вершина");
        addEdgeButton = new JButton("+ребро");

        startButton.setForeground(Color.BLUE);

        add(downloadButton);
        add(saveButton);
        add(startButton);
        add(backstepButton);
        add(nextstepButton);
        add(addVertexButton);
        add(addEdgeButton);
    }

    public void setAddVertexButtonListener(ActionListener listener){
        addVertexButton.addActionListener(listener);
    }

    public void setAddEdgeButtonListener(ActionListener listener){
        addEdgeButton.addActionListener(listener);
    }

    public void setDownloadButton(ActionListener listener){
        downloadButton.addActionListener(listener);
    }

    public void setSaveButton(ActionListener listener){
        saveButton.addActionListener(listener);
    }

    public void setStartButton(ActionListener listener){
        startButton.addActionListener(listener);
    }

    public void setBackstepButton(ActionListener listener){
        backstepButton.addActionListener(listener);
    }

    public void setNextstepButton(ActionListener listener){
        nextstepButton.addActionListener(listener);
    }
}
