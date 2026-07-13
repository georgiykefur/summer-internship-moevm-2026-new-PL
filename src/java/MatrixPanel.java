import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MatrixPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private Object[][] initialData = {
            {"0", "4", "5", "7", "2"},
            {"∞", "0", "1", "∞", "∞"},
            {"∞", "6", "0", "∞", "∞"},
            {"∞", "∞", "8", "0", "∞"},
            {"∞", "∞", "∞", "5", "0"}
    };;

    public MatrixPanel(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.LIGHT_GRAY));

        // Заголовок (название)
        JLabel titleLabel = new JLabel("Матрица расстояний", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15,0,5,0));
        add(titleLabel, BorderLayout.NORTH);

        // Название колонок
        String[] vertexNames = {"A", "B", "C", "D", "E"};

        tableModel = new DefaultTableModel(initialData, vertexNames){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        // Текст в ячейках по центру
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i=0;i<tableModel.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Квадратные ячейки
        int cellSize = 40;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(cellSize);

        for (int i=0;i<tableModel.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            table.getColumnModel().getColumn(i).setMinWidth(cellSize);
            table.getColumnModel().getColumn(i).setMaxWidth(cellSize);
        }

        Dimension headerSize = table.getTableHeader().getPreferredSize();
        headerSize.height = cellSize;
        table.getTableHeader().setPreferredSize(headerSize);

        table.setPreferredScrollableViewportSize(new Dimension(cellSize * vertexNames.length, cellSize * initialData.length));

        // Создание скролл панели
        JScrollPane scrollPane = new JScrollPane(table);

//         Имена вершин слева от матрицы
        JList<String> rowHeader = new JList<>(vertexNames);
        rowHeader.setFixedCellHeight(cellSize);
        rowHeader.setFixedCellWidth(cellSize);

        rowHeader.setBackground(new Color(238, 238, 238));

        DefaultListCellRenderer listRenderer = (DefaultListCellRenderer) rowHeader.getCellRenderer();
        listRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        scrollPane.setRowHeaderView(rowHeader);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());


        JPanel toCenter = new JPanel(new GridBagLayout());
        toCenter.add(scrollPane);

        add(toCenter, BorderLayout.CENTER);
    }

    public void updateMatrix(int row, int col, String[] value){
        tableModel.setValueAt(value, row, col);
    }

    public void setMatrix(Object[][] mtx){
        this.initialData = mtx;
    }
}
