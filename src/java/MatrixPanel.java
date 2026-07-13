import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class MatrixPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private Object[][] initialData;
    private ArrayList<String> vertexNames;

    public MatrixPanel(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.LIGHT_GRAY));

        // Заголовок (название)
        JLabel titleLabel = new JLabel("Матрица расстояний", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15,0,5,0));
        add(titleLabel, BorderLayout.NORTH);

    }

    public void createMatrixPanel(int[][] matrix){
        initialData = convertIntToObject(matrix);
        setHeader();

        tableModel = new DefaultTableModel(initialData, vertexNames.toArray()){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        // Текст в ячейках по центру
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Квадратные ячейки
        int cellSize = 40;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(cellSize);

        for (int i=0;i<tableModel.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            table.getColumnModel().getColumn(i).setMinWidth(cellSize);
            table.getColumnModel().getColumn(i).setMaxWidth(cellSize);
        }

        // Квадратные горизонтальные ячейки названия вершин
        Dimension headerSize = table.getTableHeader().getPreferredSize();
        headerSize.height = cellSize;
        table.getTableHeader().setPreferredSize(headerSize);

        table.setPreferredScrollableViewportSize(new Dimension(cellSize * vertexNames.size(), cellSize * initialData.length));

        // Создание скролл панели
        JScrollPane scrollPane = new JScrollPane(table);

//      Имена вершин слева от матрицы
        JList<String> rowHeader = new JList<>(vertexNames.toArray(new String[0]));
        rowHeader.setFixedCellHeight(cellSize);
        rowHeader.setFixedCellWidth(cellSize);
        rowHeader.setBackground(new Color(238, 238, 238));

        rowHeader.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
                return label;
            }
        });

        scrollPane.setRowHeaderView(rowHeader);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Всю матрицу строго по центру
        JPanel toCenter = new JPanel(new GridBagLayout());
        toCenter.add(scrollPane);

        add(toCenter, BorderLayout.CENTER);
    }

    public void updateMatrix(int row, int col, String[] value){
        tableModel.setValueAt(value, row, col);
    }

    public Object[][] convertIntToObject(int[][] mtx){
        Object[][] temp = new Object[mtx.length][];
        for (int i = 0; i < mtx.length; i++) {
            temp[i] = new Object[mtx[i].length];
            for (int j = 0; j < mtx[i].length; j++) {
                temp[i][j] = mtx[i][j];
            }
        }
        return temp;
    }

    private void setHeader(){
        int n = initialData.length;
        vertexNames = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vertexNames.add(String.valueOf(i+1));
        }
    }
}
