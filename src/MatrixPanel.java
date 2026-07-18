import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class MatrixPanel extends JPanel {

    private JTable table;                                // таблица с матрицей
    private JScrollPane scrollPane;                      // скролл для таблицы
    private DefaultTableModel tableModel;                // модель данных таблицы
    private JLabel titleLabel;                           // заголовок "матрица смежности/расстояний"
    private JLabel counterLabel;                         // счётчики k, i, j
    private JPanel centerPanel;                          // панель для таблицы

    private Set<Point> highlightedCells = new HashSet<>(); // подсвеченные ячейки
    private String[] vertexNameArray;                    // имена вершин

    // интерфейс для клика по ячейке
    public interface CellClickListener {
        void onCellClicked(int from, int to);
    }

    private CellClickListener cellClickListener;

    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    public MatrixPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0,0,0,0, new Color(180, 195, 240)));

        titleLabel = new JLabel("Матрица смежности", SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        counterLabel = new JLabel(" ", SwingConstants.CENTER);
        counterLabel.setForeground(Color.WHITE);
        counterLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(titleLabel, BorderLayout.NORTH);
        northPanel.add(counterLabel, BorderLayout.SOUTH);
        northPanel.setBackground(new Color(17, 30, 94));
        northPanel.setPreferredSize(new Dimension(0, 60));

        add(northPanel, BorderLayout.NORTH);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        add(centerPanel, BorderLayout.CENTER);

        showEmptyMatrix();
    }

    public void setVertexNames(String[] names) {
        this.vertexNameArray = names;
    }

    public void renderMatrixPanel(int[][] matrix) {
        titleLabel.setText("Матрица смежности");
        renderMatrix(matrix);
    }

    public void renderMatrixPanel(double[][] matrix) {
        titleLabel.setText("Матрица расстояний");
        renderMatrix(matrix);
    }

    // отрисовка матрицы double
    private void renderMatrix(double[][] matrix) {
        int n = matrix.length;
        if (n == 0) {
            showEmptyMatrix();
            return;
        }

        String[] names = getVertexNames(n);
        this.vertexNameArray = names;

        String[][] data = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] >= Graph.INF / 2) {
                    data[i][j] = "∞";
                } else {
                    data[i][j] = String.format("%.0f", matrix[i][j]);
                }
            }
        }
        buildTable(data, names);
    }

    // отрисовка матрицы int
    private void renderMatrix(int[][] matrix) {
        int n = matrix.length;
        if (n == 0) {
            showEmptyMatrix();
            return;
        }

        String[] names = getVertexNames(n);
        this.vertexNameArray = names;

        String[][] data = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == Integer.MAX_VALUE) {
                    data[i][j] = "∞";
                } else {
                    data[i][j] = String.valueOf(matrix[i][j]);
                }
            }
        }
        buildTable(data, names);
    }

    // получить имена вершин для матрицы
    private String[] getVertexNames(int n) {
        String[] names = new String[n];
        if (vertexNameArray != null && vertexNameArray.length == n) {
            return vertexNameArray.clone();
        }
        for (int i = 0; i < n; i++) {
            names[i] = String.valueOf((char) ('A' + i));
        }
        return names;
    }

    // показать пустую матрицу
    private void showEmptyMatrix() {
        centerPanel.removeAll();
        JLabel emptyLabel = new JLabel("нет данных", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        emptyLabel.setForeground(Color.GRAY);
        centerPanel.add(emptyLabel, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // построить таблицу
    private void buildTable(String[][] data, String[] names) {
        centerPanel.removeAll();

        int n = data.length;
        if (n == 0) {
            showEmptyMatrix();
            return;
        }

        // заголовки: первая колонка пустая, остальные — имена вершин
        String[] headers = new String[n + 1];
        headers[0] = "";
        for (int i = 0; i < n; i++) {
            headers[i + 1] = names[i];
        }

        // данные: первая колонка — имена вершин, остальные — матрица
        String[][] tableData = new String[n][n + 1];
        for (int i = 0; i < n; i++) {
            tableData[i][0] = names[i];
            for (int j = 0; j < n; j++) {
                tableData[i][j + 1] = data[i][j];
            }
        }

        tableModel = new DefaultTableModel(tableData, headers) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        // обработчик клика по ячейке
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (row >= 0 && column > 0 && cellClickListener != null) {
                    int from = row;
                    int to = column - 1;
                    if (from < names.length && to < names.length) {
                        cellClickListener.onCellClicked(from, to);
                    }
                }
            }
        });

        // центрирование ячеек и подсветка
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (column > 0 && highlightedCells.contains(new Point(row, column - 1))) {
                    setBackground(new Color(255, 230, 0)); // жёлтый
                } else {
                    setBackground(Color.WHITE);
                }
                return this;
            }
        };

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        int cellSize = 35;

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(cellSize);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            table.getColumnModel().getColumn(i).setMinWidth(cellSize);
            table.getColumnModel().getColumn(i).setMaxWidth(cellSize);
        }

        Dimension headerSize = table.getTableHeader().getPreferredSize();
        headerSize.height = cellSize;
        table.getTableHeader().setPreferredSize(headerSize);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBackground(new Color(17, 30, 94));

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // установка счётчиков
    public void setCounters(int k, int i, int j) {
        if (k < 0) {
            counterLabel.setText(" ");
        } else {
            String kName = (vertexNameArray != null && k < vertexNameArray.length) ? vertexNameArray[k] : String.valueOf(k + 1);
            String iName = (vertexNameArray != null && i < vertexNameArray.length) ? vertexNameArray[i] : String.valueOf(i + 1);
            String jName = (vertexNameArray != null && j < vertexNameArray.length) ? vertexNameArray[j] : String.valueOf(j + 1);
            counterLabel.setText("k = " + kName + "   i = " + iName + "   j = " + jName);
        }
    }

    // подсветка улучшенных ячеек
    public void highlightCells(List<FloydWarshall.EdgeUpdate> updates) {
        highlightedCells.clear();
        if (updates == null || table == null) return;
        for (FloydWarshall.EdgeUpdate u : updates) {
            if (u.improved) {
                highlightedCells.add(new Point(u.from, u.to));
            }
        }
        if (table != null) table.repaint();
    }

    public void clearHighlight() {
        highlightedCells.clear();
        counterLabel.setText(" ");
        if (table != null) table.repaint();
    }
}