import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {

    class Vertex {
        static int count = 0;
        int x, y;
        int id;
        String name; 

        public Vertex(int x, int y, String name) {
            Vertex.count++;
            this.id = Vertex.count;
            this.x = x;
            this.y = y;
            this.name = name;
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
    private final int RADIUS = 22;
    private Color basicColorVertex = new Color(130,130,130);
    private int offsetX = 0, offsetY = 0;

    private int highlightK = -1; // красный
    private int highlightI = -1; // синий
    private int highlightJ = -1; // зеленый

    private final Set<Integer> improvedEdges = new HashSet<>();
    private final Set<Integer> newEdges = new HashSet<>();
    private final List<Edge> virtualEdges = new ArrayList<>();

    
    private Map<Integer, String> vertexNameMap = new HashMap<>();

    public GraphPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
    }

    public void setVertexNames(String[] names) {
        vertexNameMap.clear();
        for (int i = 0; i < names.length; i++) {
            vertexNameMap.put(i, names[i]);
        }
        
        for (int i = 0; i < vertices.size() && i < names.length; i++) {
            vertices.get(i).name = names[i];
        }
        repaint();
    }

    public void setHighlight(int k, int i, int j){
        this.highlightK = k;
        this.highlightI = i;
        this.highlightJ = j;
        repaint();
    }

    public void setHighlightedEdges(List<FloydWarshall.EdgeUpdate> updates) {
        improvedEdges.clear();
        newEdges.clear();
        virtualEdges.clear();
        if (updates == null) {
            repaint();
            return;
        }
        for (FloydWarshall.EdgeUpdate update : updates) {
            if (!update.improved) continue;

            boolean found = false;
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                int fromIdx = e.from.id - 1;
                int toIdx   = e.to.id - 1;
                if (fromIdx == update.from && toIdx == update.to) {
                    if (update.wasINF) {
                        newEdges.add(i);
                    } else {
                        improvedEdges.add(i);
                    }
                    found = true;
                    break;
                }
            }

            if (!found && update.wasINF) {
                Vertex from = vertices.get(update.from);
                Vertex to   = vertices.get(update.to);
                virtualEdges.add(new Edge(from, to, (int) update.value));
            }
        }
        repaint();
    }

    public void clearHighlights(){
        highlightK = -1;
        highlightI = -1;
        highlightJ = -1;
        improvedEdges.clear();
        newEdges.clear();
        virtualEdges.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(offsetX, offsetY);

        for (int idx = 0; idx < edges.size(); idx++) {
            Edge edge = edges.get(idx);
            boolean improved = improvedEdges.contains(idx);
            boolean isNew    = newEdges.contains(idx);
            drawDirectedEdge(g2d, edge.from, edge.to, edge.weight, improved, isNew);
        }

        for (Edge edge : virtualEdges) {
            drawDirectedEdge(g2d, edge.from, edge.to, edge.weight, false, true);
        }

        for (Vertex vertex : vertices) {
            drawVertex(g2d, vertex);
        }
    }

    private void drawVertex(Graphics2D g2d, Vertex vertex) {
        int idx = vertex.id - 1;

        Color fillColor;
        Color borderColor = null;

        if (idx == highlightK) {
            fillColor   = new Color(200, 50, 50);   // красный — k
            borderColor = new Color(150, 0, 0);
        } else if (idx == highlightI) {
            fillColor   = new Color(50, 100, 200);  // синий — i
            borderColor = new Color(0, 0, 160);
        } else if (idx == highlightJ) {
            fillColor   = new Color(50, 170, 80);   // зелёный — j
            borderColor = new Color(0, 120, 0);
        } else {
            fillColor = basicColorVertex;    // обычный
        }

        g2d.setColor(fillColor);
        g2d.fillOval(vertex.x - RADIUS, vertex.y - RADIUS, RADIUS * 2, RADIUS * 2);

        if (borderColor != null) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(vertex.x - RADIUS, vertex.y - RADIUS, RADIUS * 2, RADIUS * 2);
            g2d.setStroke(new BasicStroke(2));
        }

        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = vertex.name != null ? vertex.name : String.valueOf(vertex.id);
        int textWidth  = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, vertex.x - textWidth / 2, vertex.y + textHeight / 2 - 2);
    }

    private void drawDirectedEdge(Graphics2D g2d, Vertex v1, Vertex v2,
                                  int weight, boolean improved, boolean isNew){
        int x1 = v1.x;
        int y1 = v1.y;
        int x2 = v2.x;
        int y2 = v2.y;

        double midX = (x1+x2)/2.0;
        double midY = (y1+y2)/2.0;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx*dx + dy*dy);

        double curveFactor = 33.0;

        double normalX = -dy / length;
        double normalY = dx / length;

        double ctrlX = midX + normalX*curveFactor;
        double ctrlY = midY + normalY*curveFactor;

        Color  edgeColor;
        Stroke edgeStroke;

        if (improved) {
            edgeColor  = new Color(0, 200, 0);
            edgeStroke = new BasicStroke(4);
        } else if (isNew) {
            edgeColor  = new Color(0, 180, 0);
            edgeStroke = new BasicStroke(
                    2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[]{8, 5}, 0);
        } else {
            edgeColor  = Color.GRAY;
            edgeStroke = new BasicStroke(2);
        }

        g2d.setColor(edgeColor);
        g2d.setStroke(edgeStroke);
        g2d.draw(new QuadCurve2D.Double(x1, y1, ctrlX, ctrlY, x2, y2));


        double labelX = 0.25*x1 + 0.5*ctrlX + 0.25*x2;
        double labelY = 0.25*y1 + 0.5*ctrlY + 0.25*y2;

        labelX += normalX * 8;
        labelY += normalY * 8;

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor((improved || isNew) ? new Color(0, 140, 0) : Color.BLACK);

        FontMetrics fm = g2d.getFontMetrics();
        String weightText = String.valueOf(weight);
        int textW = fm.stringWidth(weightText);
        int textH = fm.getAscent();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(
                (int) labelX - textW / 2 - 2,
                (int) labelY - textH / 2 - 2,
                textW + 4,
                textH + 4
        );

        g2d.setColor((improved || isNew) ? new Color(0, 140, 0) : Color.BLACK);
        g2d.drawString(weightText,
                (int) labelX - textW / 2,
                (int) labelY + textH / 2 - 2);


        double angle  = Math.atan2(y2 - ctrlY, x2 - ctrlX);
        double arrowX = x2 - RADIUS * Math.cos(angle);
        double arrowY = y2 - RADIUS * Math.sin(angle);

        Polygon arrow = new Polygon();
        arrow.addPoint(0,   0);
        arrow.addPoint(-10, -5);
        arrow.addPoint(-10,  5);

        AffineTransform af = new AffineTransform();
        af.translate(arrowX, arrowY);
        af.rotate(angle);

        AffineTransform old = g2d.getTransform();
        g2d.transform(af);
        g2d.setColor(edgeColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.fill(arrow);
        g2d.setTransform(old);
    }

    public void addVertex(int x, int y, String name) {
        vertices.add(new Vertex(x, y, name));
        repaint();
    }

    public void addVertex(int x, int y) {
        String name = String.valueOf((char) ('A' + vertices.size()));
        vertices.add(new Vertex(x, y, name));
        repaint();
    }

    public void addVertex() {
        if (!vertices.isEmpty()) {
            Vertex v = vertices.getLast();
            String name = String.valueOf((char) ('A' + vertices.size()));
            vertices.add(new Vertex(v.x + 15, v.y + 15, name));
        } else {
            vertices.add(new Vertex(100, 100, "A"));
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
        improvedEdges.clear();
        newEdges.clear();
        virtualEdges.clear();
        highlightK = -1;
        highlightI = -1;
        highlightJ = -1;
        vertexNameMap.clear();
        Vertex.count = 0;
        repaint();
    }

    public int getCount() {
        return Vertex.count;
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public String getVertexName(int index) {
        if (index >= 0 && index < vertices.size()) {
            return vertices.get(index).name;
        }
        return String.valueOf(index);
    }
}