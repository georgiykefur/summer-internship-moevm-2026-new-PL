import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {

    // внутренние классы для вершин и рёбер
    class Vertex {
        int x, y;
        int index;
        String name;

        public Vertex(int x, int y, String name, int index) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.index = index;
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

    // списки вершин и рёбер
    private List<Vertex> vertices = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private final int RADIUS = 22;
    private Color basicColorVertex = new Color(130,130,130);
    private GuiController controller;

    // параметры камеры для зума и панорамирования
    private double camX = 0, camY = 0;
    private double zoom = 1.0;
    private static final double ZOOM_MIN = 0.2;
    private static final double ZOOM_MAX = 4.0;
    private int dragStartX, dragStartY;
    private double camStartX, camStartY;

    // режим добавления ребра
    private Vertex edgeFrom = null;

    // перетаскивание
    private boolean draggingBackground = false;
    private Vertex draggingVertex = null;
    private double dragVertexOffsetX, dragVertexOffsetY;

    private boolean didDrag = false;
    private static final int DRAG_THRESHOLD = 4;

    // подсветка для пошагового алгоритма (k, i, j)
    private int highlightK = -1;
    private int highlightI = -1;
    private int highlightJ = -1;

    // подсветка улучшенных/новых рёбер
    private final Set<Integer> improvedEdges = new HashSet<>();
    private final Set<Integer> newEdges = new HashSet<>();
    private final List<Edge> virtualEdges = new ArrayList<>();

    // подсветка пути (клик по ячейке матрицы)
    private final Set<Integer> pathVertices = new HashSet<>();
    private final Set<Integer> pathEdges = new HashSet<>();
    private static final Color PATH_COLOR = new Color(255, 140, 0);

    private Map<Integer, String> vertexNameMap = new HashMap<>();

    public GraphPanel() {
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                didDrag = false;
                Vertex v = vertexAt(e.getX(), e.getY());

                if (v != null) {
                    draggingVertex = v;
                    Point2D world = screenToWorld(e.getX(), e.getY());
                    dragVertexOffsetX = world.getX() - v.x;
                    dragVertexOffsetY = world.getY() - v.y;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    draggingBackground = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    camStartX = camX;
                    camStartY = camY;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingVertex != null) {
                    Point2D world = screenToWorld(e.getX(), e.getY());
                    double dx = world.getX() - dragVertexOffsetX - draggingVertex.x;
                    double dy = world.getY() - dragVertexOffsetY - draggingVertex.y;
                    if (!didDrag && Math.sqrt(dx*dx + dy*dy) < DRAG_THRESHOLD) return;

                    didDrag = true;
                    draggingVertex.x = (int)(world.getX() - dragVertexOffsetX);
                    draggingVertex.y = (int)(world.getY() - dragVertexOffsetY);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();

                } else if (draggingBackground) {
                    int dx = e.getX() - dragStartX;
                    int dy = e.getY() - dragStartY;
                    if (!didDrag && Math.sqrt(dx*dx + dy*dy) < DRAG_THRESHOLD) return;

                    didDrag = true;
                    camX = camStartX + (e.getX() - dragStartX) / zoom;
                    camY = camStartY + (e.getY() - dragStartY) / zoom;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoom;
                double delta = e.getPreciseWheelRotation() * 0.06;
                double factor = 1.0 - delta;
                zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom * factor));

                // точка под курсором не прыгает
                double mouseX = e.getX();
                double mouseY = e.getY();
                camX = mouseX / zoom - mouseX / oldZoom + camX;
                camY = mouseY / zoom - mouseY / oldZoom + camY;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (controller != null && controller.isAlgorithmRunning()) return;

                if (!didDrag) {
                    Vertex v = vertexAt(e.getX(), e.getY());

                    if (edgeFrom != null) {
                        // режим добавления ребра
                        if (v != null && v != edgeFrom) {
                            int fromIndex = getRealIndex(edgeFrom);
                            int toIndex = getRealIndex(v);

                            if (fromIndex == -1 || toIndex == -1) {
                                JOptionPane.showMessageDialog(GraphPanel.this,
                                        "Ошибка: не удалось определить индексы вершин",
                                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                            } else {
                                Edge existing = edges.stream()
                                        .filter(edge -> edge.from == edgeFrom && edge.to == v)
                                        .findFirst().orElse(null);

                                if (existing != null) {
                                    showEdgeMenu(existing, e.getX(), e.getY());
                                } else {
                                    String input = JOptionPane.showInputDialog(
                                            GraphPanel.this,
                                            "Вес ребра " + edgeFrom.name + " → " + v.name + ":",
                                            "Добавить ребро",
                                            JOptionPane.PLAIN_MESSAGE
                                    );
                                    if (input != null) {
                                        try {
                                            int weight = Integer.parseInt(input.trim());
                                            controller.addEdge(fromIndex, toIndex, weight);
                                        } catch (NumberFormatException ex) {
                                            JOptionPane.showMessageDialog(GraphPanel.this, "Введи целое число");
                                        }
                                    }
                                }
                            }
                        }
                        edgeFrom = null;
                        setCursor(Cursor.getDefaultCursor());
                    } else {
                        // обычный клик
                        if (v != null) {
                            showVertexMenu(v, e.getX(), e.getY());
                        } else {
                            Edge edge = edgeAt(e.getX(), e.getY());
                            if (edge != null) {
                                showEdgeMenu(edge, e.getX(), e.getY());
                            } else {
                                setCursor(Cursor.getDefaultCursor());
                            }
                        }
                    }
                }

                draggingVertex = null;
                draggingBackground = false;
                didDrag = false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (edgeFrom != null) {
                    repaint();
                }
                Vertex v = vertexAt(e.getX(), e.getY());
                if (v != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (edgeFrom != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    Edge edge = edgeAt(e.getX(), e.getY());
                    if (edge != null) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // двойной клик для добавления вершины
                if (e.getClickCount() == 2 && controller != null) {
                    Point2D world = screenToWorld(e.getX(), e.getY());
                    controller.addVertex((int)world.getX(), (int)world.getY());
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    // получить реальный индекс вершины в списке
    private int getRealIndex(Vertex v) {
        if (v == null) return -1;
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i) == v) {
                return i;
            }
        }
        return v.index;
    }

    // найти индекс ребра по индексам вершин
    private int findEdgeIndex(int fromIndex, int toIndex) {
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            int fromIdx = getRealIndex(e.from);
            int toIdx = getRealIndex(e.to);
            if (fromIdx == fromIndex && toIdx == toIndex) {
                return i;
            }
        }
        return -1;
    }

    public void removeEdge(int fromIndex, int toIndex) {
        int idx = findEdgeIndex(fromIndex, toIndex);
        if (idx != -1) {
            edges.remove(idx);
            repaint();
        }
    }

    public void updateEdgeWeight(int fromIndex, int toIndex, int newWeight) {
        int idx = findEdgeIndex(fromIndex, toIndex);
        if (idx != -1) {
            edges.get(idx).weight = newWeight;
            repaint();
        }
    }

    // контекстное меню для вершины
    private void showVertexMenu(Vertex v, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem addEdgeItem = new JMenuItem("Добавить ребро от " + v.name);
        addEdgeItem.addActionListener(e -> startAddEdge(v));

        JMenuItem deleteItem = new JMenuItem("Удалить вершину " + v.name);
        deleteItem.addActionListener(e -> {
            if (controller != null) {
                int idx = getRealIndex(v);
                if (idx != -1) {
                    controller.removeVertex(idx);
                }
            }
        });

        menu.add(addEdgeItem);
        menu.add(deleteItem);
        menu.show(this, x, y);
    }

    // контекстное меню для ребра
    private void showEdgeMenu(Edge edge, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        int fromIdx = getRealIndex(edge.from);
        int toIdx = getRealIndex(edge.to);

        JMenuItem editItem = new JMenuItem("Изменить вес " + edge.from.name + " → " + edge.to.name + " (" + edge.weight + ")");
        editItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(GraphPanel.this,
                    "Новый вес ребра " + edge.from.name + " → " + edge.to.name + ":",
                    "Изменить вес",
                    JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                try {
                    int newWeight = Integer.parseInt(input.trim());
                    if (controller != null && fromIdx != -1 && toIdx != -1) {
                        controller.updateEdgeWeight(fromIdx, toIdx, newWeight);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GraphPanel.this, "Введи целое число");
                }
            }
        });

        JMenuItem deleteItem = new JMenuItem("Удалить ребро " + edge.from.name + " → " + edge.to.name);
        deleteItem.addActionListener(e -> {
            if (controller != null && fromIdx != -1 && toIdx != -1) {
                controller.removeEdge(fromIdx, toIdx);
            }
        });

        menu.add(editItem);
        menu.add(deleteItem);
        menu.show(this, x, y);
    }

    public void removeVertex(int index) {
        if (index < 0 || index >= vertices.size()) return;

        Vertex toRemove = vertices.get(index);
        edges.removeIf(edge -> edge.from == toRemove || edge.to == toRemove);
        vertices.remove(index);
        reindexVertices();

        repaint();
    }

    public void setController(GuiController controller) {
        this.controller = controller;
    }

    private void startAddEdge(Vertex from) {
        edgeFrom = from;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void applyZoomStep(double factor) {
        double oldZoom = zoom;
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom * factor));

        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;

        camX = cx / zoom - cx / oldZoom + camX;
        camY = cy / zoom - cy / oldZoom + camY;

        repaint();
    }

    // преобразования координат (экран <-> мир)
    private AffineTransform worldToScreen() {
        AffineTransform at = new AffineTransform();
        at.scale(zoom, zoom);
        at.translate(camX, camY);
        return at;
    }

    private Point2D screenToWorld(int sx, int sy) {
        AffineTransform at = worldToScreen();
        try {
            Point2D result = at.inverseTransform(new Point2D.Double(sx, sy), null);
            return result;
        } catch (java.awt.geom.NoninvertibleTransformException ex) {
            return new Point2D.Double(sx, sy);
        }
    }

    // поиск вершины по координатам
    private Vertex vertexAt(int sx, int sy) {
        Point2D world = screenToWorld(sx, sy);
        double wx = world.getX(), wy = world.getY();
        for (int i = vertices.size() - 1; i >= 0; i--) {
            Vertex v = vertices.get(i);
            double dx = v.x - wx, dy = v.y - wy;
            if (dx * dx + dy * dy <= RADIUS * RADIUS) return v;
        }
        return null;
    }

    // поиск ребра по координатам
    private Edge edgeAt(int sx, int sy) {
        Point2D world = screenToWorld(sx, sy);
        double wx = world.getX(), wy = world.getY();

        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            double x1 = edge.from.x, y1 = edge.from.y;
            double x2 = edge.to.x, y2 = edge.to.y;

            double midX = (x1 + x2) / 2.0;
            double midY = (y1 + y2) / 2.0;
            double dx = x2 - x1, dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length == 0) continue;
            double curveFactor = 33.0;
            double normalX = -dy / length;
            double normalY = dx / length;
            double ctrlX = midX + normalX * curveFactor;
            double ctrlY = midY + normalY * curveFactor;

            // проверяем точки вдоль кривой Безье
            for (double t = 0; t <= 1; t += 0.02) {
                double bx = (1-t)*(1-t)*x1 + 2*(1-t)*t*ctrlX + t*t*x2;
                double by = (1-t)*(1-t)*y1 + 2*(1-t)*t*ctrlY + t*t*y2;
                double dist = Math.sqrt((wx - bx) * (wx - bx) + (wy - by) * (wy - by));
                if (dist <= 6) return edge;
            }
        }
        return null;
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

    // установка подсветки для пошагового алгоритма
    public void setHighlight(int k, int i, int j) {
        this.highlightK = k;
        this.highlightI = i;
        this.highlightJ = j;
        repaint();
    }

    // установка подсветки улучшенных рёбер
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
                int fromIdx = getRealIndex(e.from);
                int toIdx = getRealIndex(e.to);
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
                Vertex to = vertices.get(update.to);
                virtualEdges.add(new Edge(from, to, (int) update.value));
            }
        }
        repaint();
    }

    public void clearHighlights() {
        highlightK = -1;
        highlightI = -1;
        highlightJ = -1;
        improvedEdges.clear();
        newEdges.clear();
        virtualEdges.clear();
        repaint();
    }

    // подсветка пути
    public void showPath(List<Integer> vertexIndices, List<int[]> edgePairs) {
        pathVertices.clear();
        pathEdges.clear();

        if (vertexIndices == null || vertexIndices.isEmpty()) {
            repaint();
            return;
        }

        pathVertices.addAll(vertexIndices);

        if (edgePairs != null) {
            for (int[] pair : edgePairs) {
                if (pair == null || pair.length < 2) continue;
                int fromIdx = pair[0];
                int toIdx = pair[1];

                for (int idx = 0; idx < edges.size(); idx++) {
                    Edge e = edges.get(idx);
                    int eFrom = getRealIndex(e.from);
                    int eTo = getRealIndex(e.to);
                    if (eFrom == fromIdx && eTo == toIdx) {
                        pathEdges.add(idx);
                        break;
                    }
                }
            }
        }
        repaint();
    }

    public void clearPath() {
        pathVertices.clear();
        pathEdges.clear();
        repaint();
    }

    // синхронизация координат с graph для сохранения
    public int getVertexX(int index) {
        if (index >= 0 && index < vertices.size()) {
            return vertices.get(index).x;
        }
        return -1;
    }

    public int getVertexY(int index) {
        if (index >= 0 && index < vertices.size()) {
            return vertices.get(index).y;
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.transform(worldToScreen());

        // рисуем рёбра
        for (int idx = 0; idx < edges.size(); idx++) {
            Edge edge = edges.get(idx);
            boolean improved = improvedEdges.contains(idx);
            boolean isNew = newEdges.contains(idx);
            boolean isPath = pathEdges.contains(idx);
            drawDirectedEdge(g2d, edge.from, edge.to, edge.weight, improved, isNew, isPath);
        }

        // виртуальные рёбра
        for (Edge edge : virtualEdges) {
            drawDirectedEdge(g2d, edge.from, edge.to, edge.weight, false, true, false);
        }

        // вершины
        for (Vertex vertex : vertices) {
            drawVertex(g2d, vertex);
        }

        // пунктирная линия при добавлении ребра
        if (edgeFrom != null) {
            g2d.setColor(new Color(100, 100, 255, 180));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{6, 4}, 0));
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouse, this);
            Point2D world = screenToWorld(mouse.x, mouse.y);
            g2d.drawLine(edgeFrom.x, edgeFrom.y, (int)world.getX(), (int)world.getY());
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawVertex(Graphics2D g2d, Vertex vertex) {
        int idx = getRealIndex(vertex);

        Color fillColor;
        Color borderColor = null;

        if (idx == highlightK) {
            fillColor = new Color(200, 50, 50);   // красный
            borderColor = new Color(150, 0, 0);
        } else if (idx == highlightI) {
            fillColor = new Color(50, 100, 200);  // синий
            borderColor = new Color(0, 0, 160);
        } else if (idx == highlightJ) {
            fillColor = new Color(50, 170, 80);   // зелёный
            borderColor = new Color(0, 120, 0);
        } else {
            fillColor = basicColorVertex;
        }

        g2d.setColor(fillColor);
        g2d.fillOval(vertex.x - RADIUS, vertex.y - RADIUS, RADIUS * 2, RADIUS * 2);

        if (borderColor != null) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(vertex.x - RADIUS, vertex.y - RADIUS, RADIUS * 2, RADIUS * 2);
            g2d.setStroke(new BasicStroke(2));
        }

        // оранжевая обводка для пути
        if (pathVertices.contains(idx)) {
            g2d.setColor(PATH_COLOR);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(vertex.x - RADIUS - 4, vertex.y - RADIUS - 4,
                    (RADIUS + 4) * 2, (RADIUS + 4) * 2);
            g2d.setStroke(new BasicStroke(2));
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = vertex.name != null ? vertex.name : String.valueOf(vertex.index);
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, vertex.x - textWidth / 2, vertex.y + textHeight / 2 - 2);
    }

    private void drawDirectedEdge(Graphics2D g2d, Vertex v1, Vertex v2,
                                  int weight, boolean improved, boolean isNew, boolean isPath) {
        int x1 = v1.x;
        int y1 = v1.y;
        int x2 = v2.x;
        int y2 = v2.y;

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        double curveFactor = 33.0;

        double normalX = -dy / length;
        double normalY = dx / length;

        double ctrlX = midX + normalX * curveFactor;
        double ctrlY = midY + normalY * curveFactor;

        Color edgeColor;
        Stroke edgeStroke;

        if (isPath) {
            edgeColor = PATH_COLOR;
            edgeStroke = new BasicStroke(4);
        } else if (improved) {
            edgeColor = new Color(0, 200, 0);
            edgeStroke = new BasicStroke(4);
        } else if (isNew) {
            edgeColor = new Color(0, 180, 0);
            edgeStroke = new BasicStroke(
                    2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[]{8, 5}, 0);
        } else {
            edgeColor = Color.GRAY;
            edgeStroke = new BasicStroke(2);
        }

        g2d.setColor(edgeColor);
        g2d.setStroke(edgeStroke);
        g2d.draw(new QuadCurve2D.Double(x1, y1, ctrlX, ctrlY, x2, y2));

        // вес ребра
        double labelX = 0.25 * x1 + 0.5 * ctrlX + 0.25 * x2;
        double labelY = 0.25 * y1 + 0.5 * ctrlY + 0.25 * y2;

        labelX += normalX * 8;
        labelY += normalY * 8;

        g2d.setStroke(new BasicStroke(2));
        Color labelColor = isPath ? PATH_COLOR : ((improved || isNew) ? new Color(0, 140, 0) : Color.BLACK);
        g2d.setColor(labelColor);

        FontMetrics fm = g2d.getFontMetrics();
        String weightText = String.valueOf(weight);
        int textW = fm.stringWidth(weightText);
        int textH = fm.getAscent();

        // белый фон для веса
        g2d.setColor(Color.WHITE);
        g2d.fillRect(
                (int) labelX - textW / 2 - 2,
                (int) labelY - textH / 2 - 2,
                textW + 4,
                textH + 4
        );

        g2d.setColor(labelColor);
        g2d.drawString(weightText,
                (int) labelX - textW / 2,
                (int) labelY + textH / 2 - 2);

        // стрелка
        double angle = Math.atan2(y2 - ctrlY, x2 - ctrlX);
        double arrowX = x2 - RADIUS * Math.cos(angle);
        double arrowY = y2 - RADIUS * Math.sin(angle);

        Polygon arrow = new Polygon();
        arrow.addPoint(0, 0);
        arrow.addPoint(-10, -5);
        arrow.addPoint(-10, 5);

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
        vertices.add(new Vertex(x, y, name, vertices.size()));
        repaint();
    }

    public void addVertex(int x, int y) {
        String name = String.valueOf((char) ('A' + vertices.size()));
        addVertex(x, y, name);
    }

    public void addVertex() {
        if (!vertices.isEmpty()) {
            Vertex v = vertices.getLast();
            String name = String.valueOf((char) ('A' + vertices.size()));
            addVertex(v.x + 15, v.y + 15, name);
        } else {
            addVertex(100, 100, "A");
        }
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
        pathVertices.clear();
        pathEdges.clear();
        highlightK = -1;
        highlightI = -1;
        highlightJ = -1;
        vertexNameMap.clear();
        edgeFrom = null;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    private void reindexVertices() {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).index = i;
        }
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