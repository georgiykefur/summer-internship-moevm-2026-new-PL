import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {

    class Vertex {
        static int count = 0;
        int x, y;
        int id;

        public Vertex(int x, int y) {
            Vertex.count++;
            this.id = Vertex.count;
            this.x = x;
            this.y = y;
        }
    }

    class Edge {
        Vertex from, to;
        int weight;

        public Edge(Vertex from, Vertex to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    private List<Vertex> vertices = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private final int RADIUS = 20;
    private int offsetX = 0, offsetY = 0;

    public GraphPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(offsetX, offsetY);

        g2d.setStroke(new BasicStroke(2));
        for (Edge edge : edges) {
            g2d.setColor(Color.GRAY);
            g2d.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

            g2d.setColor(Color.BLACK);
            int midX = (edge.from.x + edge.to.x) / 2;
            int midY = (edge.from.y + edge.to.y) / 2;
            g2d.drawString(String.valueOf(edge.weight), midX, midY - 5);
        }

        for (Vertex vertex : vertices) {
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillOval(vertex.x - RADIUS, vertex.y - RADIUS, RADIUS * 2, RADIUS * 2);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));

            FontMetrics fm = g2d.getFontMetrics();
            String text = String.valueOf(vertex.id);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            g2d.drawString(text, vertex.x - textWidth / 2, vertex.y + textHeight / 2 - 2);
        }
    }

    public void addVertex(int x, int y) {
        vertices.add(new Vertex(x, y));
        repaint();
    }

    public void addVertex() {
        if (!vertices.isEmpty()) {
            Vertex v = vertices.getLast();
            vertices.add(new Vertex(v.x + 15, v.y + 15));
        } else {
            vertices.add(new Vertex(100, 100));
        }
        repaint();
    }

    public void addEdge(int from, int to, int weight) {
        if (from < vertices.size() && to < vertices.size()) {
            edges.add(new Edge(vertices.get(from), vertices.get(to), weight));
            repaint();
        }
    }

    public void clearGraph() {
        vertices.clear();
        edges.clear();
        Vertex.count = 0;
        repaint();
    }

    public int getCount() {
        return Vertex.count;
    }

    public int getVertexCount() {
        return vertices.size();
    }
}