import java.util.ArrayList;
import java.util.List;

public class FloydWarshall {
    private Graph graph;
    private double[][] dist;
    private int[][] next;
    private int n;

    public int currentK = 0;
    public int currentI = 0;
    public int currentJ = 0;
    public boolean finished = false;

    public List<EdgeUpdate> lastUpdates = new ArrayList<>();

    public FloydWarshall(Graph graph) {
        this.graph = graph;
    }

    public void init() {
        n = graph.size();
        dist = new double[n][n];
        next = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = graph.matrix[i][j];
                if (i != j && dist[i][j] < Graph.INF) {
                    next[i][j] = j;
                } else {
                    next[i][j] = -1;
                }
            }
        }

        currentK = 0;
        currentI = 0;
        currentJ = 0;
        finished = false;
        lastUpdates.clear();
    }

    public boolean step() {
        lastUpdates.clear();

        if (finished) {
            return false;
        }

        if (currentI == currentJ) {
            moveNext();
            return step();
        }

        double viaK = dist[currentI][currentK] + dist[currentK][currentJ];
        double direct = dist[currentI][currentJ];

        boolean improved = false;

        if (dist[currentI][currentK] < Graph.INF / 2 &&
                dist[currentK][currentJ] < Graph.INF / 2 &&
                viaK < direct) {

            dist[currentI][currentJ] = viaK;
            next[currentI][currentJ] = next[currentI][currentK];
            improved = true;
            lastUpdates.add(new EdgeUpdate(currentI, currentJ, viaK, true, direct >= Graph.INF / 2));
        } else {
            lastUpdates.add(new EdgeUpdate(currentI, currentJ, direct, false, false));
        }

        moveNext();
        return improved;
    }

    private void moveNext() {
        currentJ++;
        if (currentJ >= n) {
            currentJ = 0;
            currentI++;
            if (currentI >= n) {
                currentI = 0;
                currentK++;
                if (currentK >= n) {
                    finished = true;
                }
            }
        }
    }

    public void runFull() {
        init();
        while (!finished) {
            step();
        }
    }

    public double[][] getDistMatrix() {
        return dist;
    }

    public List<String> reconstructPath(int from, int to) {
        List<String> path = new ArrayList<>();
        if (dist[from][to] >= Graph.INF / 2) {
            return path;
        }
        path.add(graph.getVertex(from).id);
        int current = from;
        while (current != to) {
            int nextVertex = next[current][to];
            if (nextVertex == -1) {
                return new ArrayList<>();
            }
            path.add(graph.getVertex(nextVertex).id);
            current = nextVertex;
        }
        return path;
    }

    public List<String> reconstructPath(String fromId, String toId) {
        int from = graph.findIndex(fromId);
        int to = graph.findIndex(toId);
        if (from == -1 || to == -1) {
            return new ArrayList<>();
        }
        return reconstructPath(from, to);
    }

    public boolean hasNegativeCycle() {
        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0) {
                return true;
            }
        }
        return false;
    }

    public static class EdgeUpdate {
        public int from;
        public int to;
        public double value;
        public boolean improved;
        public boolean wasINF;

        public EdgeUpdate(int from, int to, double value, boolean improved, boolean wasINF) {
            this.from = from;
            this.to = to;
            this.value = value;
            this.improved = improved;
            this.wasINF = wasINF;
        }
    }
}