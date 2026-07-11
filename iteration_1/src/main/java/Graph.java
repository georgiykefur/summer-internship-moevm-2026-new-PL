import java.util.Arrays;

/**
 * Модель графа (Харченко Я.К., итерация 1).
 *
 * Хранит координаты вершин и матрицу весов рёбер (dist), полученную парсингом
 * файла в формате из контракта проекта (см. {@code docs/specification.md} §2
 * и {@code data/sample_graph.txt}). Отсутствующее ребро — {@link Double#POSITIVE_INFINITY}.
 *
 * Это самостоятельная модель именно для задачи Харченко (загрузка + отображение
 * матрицы). Она сознательно не связана с моделью/алгоритмом Гриценко — части
 * команды на этом этапе не интегрируются, см. {@code CLAUDE.md}.
 */
public final class Graph {

    private final int[] x;
    private final int[] y;
    private final double[][] dist;

    public Graph(int[] x, int[] y, double[][] dist) {
        int n = x.length;
        if (y.length != n) {
            throw new IllegalArgumentException(
                    "Количество координат y (" + y.length + ") не совпадает с количеством вершин (" + n + ")");
        }
        if (dist.length != n) {
            throw new IllegalArgumentException(
                    "Матрица весов должна иметь " + n + " строк, а имеет " + dist.length);
        }
        for (int i = 0; i < n; i++) {
            if (dist[i].length != n) {
                throw new IllegalArgumentException(
                        "Строка " + i + " матрицы весов должна иметь " + n + " столбцов, а имеет " + dist[i].length);
            }
        }

        this.x = x.clone();
        this.y = y.clone();
        this.dist = new double[n][];
        for (int i = 0; i < n; i++) {
            this.dist[i] = dist[i].clone();
        }
    }

    /** @return количество вершин графа */
    public int vertexCount() {
        return x.length;
    }

    public int getX(int vertexIndex) {
        return x[vertexIndex];
    }

    public int getY(int vertexIndex) {
        return y[vertexIndex];
    }

    /** @return вес ребра из {@code i} в {@code j}; {@link Double#POSITIVE_INFINITY}, если ребра нет */
    public double getDistance(int i, int j) {
        return dist[i][j];
    }

    /** @return защитная копия матрицы весов (изменения копии не затрагивают граф) */
    public double[][] distMatrixCopy() {
        double[][] copy = new double[dist.length][];
        for (int i = 0; i < dist.length; i++) {
            copy[i] = dist[i].clone();
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Graph{n=" + vertexCount() + ", dist=" + Arrays.deepToString(dist) + "}";
    }
}
