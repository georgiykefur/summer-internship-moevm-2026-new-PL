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
    private JLabel titleLabel;

    public MatrixPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.LIGHT_GRAY));

        titleLabel = new JLabel("Матрица смежности", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);
    }

    public void renderMatrixPanel(int[][] matrix) {
        titleLabel.setText("Матрица смежности");
        renderMatrix(convertIntToObject(matrix));
    }

    public void renderMatrixPanel(double[][] matrix) {
        titleLabel.setText("Матрица расстояний");
        renderMatrix(convertDoubleToObject(matrix));
    }

    private void renderMatrix(Object[][] data) {
        Component old = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (old != null) {
            remove(old);
        }

        initialData = data;
        setHeader();

        tableModel = new DefaultTableModel(initialData, vertexNames.toArray()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        int cellSize = 40;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(cellSize);

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            table.getColumnModel().getColumn(i).setMinWidth(cellSize);
            table.getColumnModel().getColumn(i).setMaxWidth(cellSize);
        }

        Dimension headerSize = table.getTableHeader().getPreferredSize();
        headerSize.height = cellSize;
        table.getTableHeader().setPreferredSize(headerSize);

        table.setPreferredScrollableViewportSize(new Dimension(
            cellSize * vertexNames.size(),
            cellSize * initialData.length
        ));

        JScrollPane scrollPane = new JScrollPane(table);

        JList<String> rowHeader = new JList<>(vertexNames.toArray(new String[0]));
        rowHeader.setFixedCellHeight(cellSize);
        rowHeader.setFixedCellWidth(cellSize);
        rowHeader.setBackground(new Color(238, 238, 238));

        rowHeader.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
                return label;
            }
        });

        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel toCenter = new JPanel(new GridBagLayout());
        toCenter.add(scrollPane);
        add(toCenter, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public Object[][] convertIntToObject(int[][] mtx) {
        Object[][] temp = new Object[mtx.length][];
        for (int i = 0; i < mtx.length; i++) {
            temp[i] = new Object[mtx[i].length];
            for (int j = 0; j < mtx[i].length; j++) {
                if (mtx[i][j] == Integer.MAX_VALUE) {
                    temp[i][j] = "∞";
                } else {
                    temp[i][j] = mtx[i][j];
                }
            }
        }
        return temp;
    }

    public Object[][] convertDoubleToObject(double[][] mtx) {
        Object[][] temp = new Object[mtx.length][];
        for (int i = 0; i < mtx.length; i++) {
            temp[i] = new Object[mtx[i].length];
            for (int j = 0; j < mtx[i].length; j++) {
                if (mtx[i][j] >= Graph.INF / 2) {
                    temp[i][j] = "∞";
                } else {
                    temp[i][j] = (int) mtx[i][j];
                }
            }
        }
        return temp;
    }

    private void setHeader() {
        int n = initialData.length;
        vertexNames = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vertexNames.add(String.valueOf(i + 1));
        }
    }
}