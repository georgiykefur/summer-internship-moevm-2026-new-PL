import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MatrixPanel extends JPanel {

    private JTable table;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;
    private Object[][] initialData;
    private ArrayList<String> vertexNames;
    private JLabel titleLabel;
    private JLabel counterLabel;

    private Set<Point> highlightedCells = new HashSet<>();
    private String[] vertexNameArray; // для отображения счётчиков

    public MatrixPanel() {
        setLayout(new BorderLayout());
//        setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.LIGHT_GRAY));

        titleLabel = new JLabel("Матрица смежности", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        counterLabel = new JLabel(" ", SwingConstants.CENTER);
        counterLabel.setForeground(Color.DARK_GRAY);
        counterLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(titleLabel, BorderLayout.NORTH);
        northPanel.add(counterLabel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
    }

    public void setVertexNames(String[] names) {
        this.vertexNameArray = names;
    }

    public void renderMatrixPanel(int[][] matrix) {
        titleLabel.setText("Матрица смежности");
        renderMatrix(convertIntToObject(matrix));
    }

    public void renderMatrixPanel(double[][] matrix) {
        titleLabel.setText("Матрица расстояний");
        renderMatrix(convertDoubleToObject(matrix));
    }

    public void setCounters(int k, int i, int j){
        if (k < 0) {
            counterLabel.setText(" ");
        } else {
            String kName = (vertexNameArray != null && k < vertexNameArray.length) ? vertexNameArray[k] : String.valueOf(k + 1);
            String iName = (vertexNameArray != null && i < vertexNameArray.length) ? vertexNameArray[i] : String.valueOf(i + 1);
            String jName = (vertexNameArray != null && j < vertexNameArray.length) ? vertexNameArray[j] : String.valueOf(j + 1);
            counterLabel.setText("k = " + kName + "   i = " + iName + "   j = " + jName);
        }
    }

    public void highlightCells(List<FloydWarshall.EdgeUpdate> updates) {
        highlightedCells.clear();
        if (updates == null || table == null) return;
        for (FloydWarshall.EdgeUpdate u : updates) {
            if (u.improved) {
                highlightedCells.add(new Point(u.from, u.to));
            }
        }
        table.repaint();
    }

    public void clearHighlight() {
        highlightedCells.clear();
        counterLabel.setText(" ");
        if (table != null) table.repaint();
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (highlightedCells.contains(new Point(row, column))) {
                    setBackground(new Color(255, 230, 0));
                } else {
                    setBackground(Color.WHITE);
                }
                return this;
            }
        };

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        int cellSize = 30;
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
            vertexNames.add(String.valueOf((char) ('A' + i)));
        }
        // Запоминаем для счётчиков
        vertexNameArray = vertexNames.toArray(new String[0]);
    }
}