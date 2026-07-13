public class GuiController {

    private GraphPanel graphPanel;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;

    public GuiController(GraphPanel gp, MatrixPanel mp, LogPanel lp){
        this.graphPanel = gp;
        this.matrixPanel = mp;
        this.logPanel = lp;
    }

    public void initialGraph(int[][] matrix){
        int n = matrix.length;
        int centerX = 300;
        int centerY = 300;
        int radius = 150;

        graphPanel.clearGraph();

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            graphPanel.addVertex(x, y);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] != 0 && matrix[i][j] != Integer.MAX_VALUE){
                    graphPanel.addEdge(i, j, matrix[i][j]);
                }
            }
        }

        matrixPanel.renderMatrixPanel(matrix);

        logPanel.printLog("Граф успешно загружен. Количество вершин: " + n);

        graphPanel.repaint();
    }

    public void updateMatrix(int[][] matrix){

    }
}
