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
    private JList<String> rowHeader;

    public MatrixPanel(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.LIGHT_GRAY));

        // Заголовок (название)
        JLabel titleLabel = new JLabel("Матрица расстояний", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15,0,5,0));
        add(titleLabel, BorderLayout.NORTH);

        table = new JTable();
        rowHeader = new JList<>();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel toCenter = new JPanel(new GridBagLayout());
        toCenter.add(scrollPane);
        add(toCenter, BorderLayout.CENTER);
    }

    public void renderMatrixPanel(int[][] matrix){
        Object[][] data = convertIntToObject(matrix);
        String[] columns = getHeaders(matrix.length);

        // Новая модель с запретом редактиварония
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Устанавливаются данные
        table.setModel(tableModel);
        rowHeader.setListData(columns);

        applyStyles(columns.length, data.length);
    }

    private String[] getHeaders(int size){
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = String.valueOf(i + 1);
        }
        return names;
    }

    // обновление одной ячейки
    public void updateMatrix(int row, int col, String value){
        tableModel.setValueAt(value, row, col);
    }

    public void updateMatrix(int row, int col, String[] value){
        tableModel.setValueAt(value, row, col);
    }

    public Object[][] convertIntToObject(int[][] mtx){
        Object[][] temp = new Object[mtx.length][];
        for (int i = 0; i < mtx.length; i++) {
            temp[i] = new Object[mtx[i].length];
            for (int j = 0; j < mtx[i].length; j++) {
                if (mtx[i][j] == Integer.MAX_VALUE) {
                    temp[i][j] = "∞";
                } else {
                    temp[i][j] = String.valueOf(mtx[i][j]);
                }
            }
        }
        return temp;
    }

    private void applyStyles(int colsCount, int rowsCount) {
        int cellSize = 40;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(cellSize);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            table.getColumnModel().getColumn(i).setMinWidth(cellSize);
            table.getColumnModel().getColumn(i).setMaxWidth(cellSize);
        }

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        Dimension headerSize = table.getTableHeader().getPreferredSize();
        headerSize.height = cellSize;
        table.getTableHeader().setPreferredSize(headerSize);

        table.setPreferredScrollableViewportSize(new Dimension(cellSize * colsCount, cellSize * rowsCount));

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
    }

}
