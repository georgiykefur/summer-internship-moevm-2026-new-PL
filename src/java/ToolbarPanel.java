import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolbarPanel extends JPanel {
    private JButton downloadButton;
    private JButton saveButton;
    private JButton startButton;
    private JButton backstepButton;
    private JButton nextstepButton;
    private JButton addVertexButton;
    private JButton addEdgeButton;

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
