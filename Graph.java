import java.util.ArrayList;
import java.util.List;

public class Graph {
    public static final double INF = Double.POSITIVE_INFINITY;

    public List<Vertex> vertices = new ArrayList<>();   // список вершин
    public double[][] matrix = new double[0][0];        // матрица смежности

    // добавить вершину с координатами
    public void addVertex(String id, double x, double y) {
        vertices.add(new Vertex(id, x, y));
        rebuildMatrix();
    }

    // удалить вершину по индексу
    public void removeVertex(int index) {
        if (index < 0 || index >= vertices.size()) return;
        vertices.remove(index);
        rebuildMatrixAfterRemoval(index);
    }

    // перестроить матрицу после удаления вершины
    private void rebuildMatrixAfterRemoval(int removedIndex) {
        int n = vertices.size() + 1;
        int newSize = n - 1;
        double[][] newMatrix = new double[newSize][newSize];

        int ni = 0;
        for (int i = 0; i < n; i++) {
            if (i == removedIndex) continue;
            int nj = 0;
            for (int j = 0; j < n; j++) {
                if (j == removedIndex) continue;
                newMatrix[ni][nj] = matrix[i][j];
                nj++;
            }
            ni++;
        }
        matrix = newMatrix;
    }

    // добавить ориентированное ребро
    public void addEdge(String fromId, String toId, double weight) {
        int from = findIndex(fromId);
        int to = findIndex(toId);
        if (from >= 0 && to >= 0) {
            matrix[from][to] = weight;
        }
    }

    // найти индекс вершины по id
    public int findIndex(String id) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).id.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public Vertex getVertex(int index) {
        return vertices.get(index);
    }

    public int size() {
        return vertices.size();
    }

    // перестроить матрицу при изменении числа вершин
    private void rebuildMatrix() {
        int n = vertices.size();
        double[][] newMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newMatrix[i][j] = (i == j) ? 0 : INF;
            }
        }
        for (int i = 0; i < Math.min(matrix.length, n); i++) {
            for (int j = 0; j < Math.min(matrix.length, n); j++) {
                newMatrix[i][j] = matrix[i][j];
            }
        }
        matrix = newMatrix;
    }

    // вершина графа
    public static class Vertex {
        public String id;
        public double x, y;

        public Vertex(String id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return id + " (" + (int)x + ", " + (int)y + ")";
        }
    }
}