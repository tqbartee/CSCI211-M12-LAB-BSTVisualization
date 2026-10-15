import com.zybooks.dsaj.searchtree.TreeMap;
import com.zybooks.dsaj.util.Entry;
import com.zybooks.dsaj.util.Position;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RBTreePanel<K, V> extends JPanel {
    private TreeMap<K, V> treeMap;
    private TreeAnimation<K, V> animationTree;
    private Map<Position<Entry<K, V>>, Point> nodePositions;
    private int nodeRadius = 20;
    private int horizontalGap = 40;
    private int verticalGap = 60;
    private int sentinelSize = 8;

    public RBTreePanel(TreeMap<K, V> treeMap) {
        this.treeMap = treeMap;
        if (treeMap instanceof TreeAnimation) {
            this.animationTree = (TreeAnimation<K, V>) treeMap;
        }
        this.nodePositions = new HashMap<>();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600)); // Default size, will adjust
    }

    public void setTreeMap(TreeMap<K, V> treeMap) {
        this.treeMap = treeMap;
        if (treeMap instanceof TreeAnimation) {
            this.animationTree = (TreeAnimation<K, V>) treeMap;
        } else {
            this.animationTree = null;
        }
        this.nodePositions.clear(); // Clear old positions
        revalidate();
        repaint();
    }

    private Position<Entry<K,V>> getRoot() {
        if (animationTree != null) return animationTree.root();
        return null;
    }

    private Position<Entry<K,V>> getLeft(Position<Entry<K,V>> p) {
        if (animationTree != null) return animationTree.left(p);
        return null;
    }

    private Position<Entry<K,V>> getRight(Position<Entry<K,V>> p) {
        if (animationTree != null) return animationTree.right(p);
        return null;
    }

    private Position<Entry<K,V>> getParent(Position<Entry<K,V>> p) {
        if (animationTree != null) return animationTree.parent(p);
        return null;
    }

    private boolean isExternal(Position<Entry<K,V>> p) {
        if (animationTree != null) return animationTree.isExternal(p);
        return true;
    }

    private boolean isRed(Position<Entry<K,V>> p) {
        if (animationTree != null) return animationTree.isRed(p);
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Position<Entry<K,V>> root = getRoot();
        if (treeMap != null && !treeMap.isEmpty() && root != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int treeWidth = calculateNodePositions(root, 0, 0);
            int treeHeight = calculateTreeHeight(root) * verticalGap + nodeRadius * 2;
            
            Dimension preferredSize = new Dimension(treeWidth + horizontalGap, treeHeight + verticalGap);
            if (!preferredSize.equals(getPreferredSize())) {
                setPreferredSize(preferredSize);
                revalidate();
            }

            drawTree(g2d, root, getPreferredSize().width / 2, nodeRadius);
        } else {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.GRAY);
            String message = "Tree is empty.";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(message, x, y);
        }
    }

    private int calculateTreeHeight(Position<Entry<K, V>> p) {
        if (p == null || isExternal(p)) return 0;
        return 1 + Math.max(calculateTreeHeight(getLeft(p)), calculateTreeHeight(getRight(p)));
    }

    // Helper to calculate positions for each node
    private int calculateNodePositions(Position<Entry<K, V>> p, int x, int depth) {
        if (p == null) return x;

        int currentX = x;
        if (isExternal(p)) {
            nodePositions.put(p, new Point(currentX + nodeRadius, depth * verticalGap + nodeRadius));
            return currentX + horizontalGap;
        }

        currentX = calculateNodePositions(getLeft(p), currentX, depth + 1);

        nodePositions.put(p, new Point(currentX + nodeRadius, depth * verticalGap + nodeRadius));
        currentX += horizontalGap;

        currentX = calculateNodePositions(getRight(p), currentX, depth + 1);
        return currentX;
    }

    private void drawTree(Graphics2D g2d, Position<Entry<K, V>> p, int parentX, int parentY) {
        if (p == null) return;

        Point myPoint = nodePositions.get(p);
        if (myPoint == null) { // Should not happen if positions are calculated correctly
            return;
        }

        // Draw connections
        Position<Entry<K, V>> parent = getParent(p);
        if (parent != null) {
            Point parentPoint = nodePositions.get(parent);
            if (parentPoint != null) {
                g2d.setColor(Color.BLACK);
                int y1 = parentPoint.y + (isExternal(parent) ? sentinelSize / 2 : nodeRadius);
                int y2 = myPoint.y - (isExternal(p) ? sentinelSize / 2 : nodeRadius);
                g2d.drawLine(parentPoint.x, y1, myPoint.x, y2);
            }
        }

        if (isExternal(p)) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(myPoint.x - sentinelSize / 2, myPoint.y - sentinelSize / 2, sentinelSize, sentinelSize);
            return;
        }

        // Draw node
        Color nodeColor = isRed(p) ? Color.RED : Color.BLACK;
        g2d.setColor(nodeColor);
        g2d.fillOval(myPoint.x - nodeRadius, myPoint.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

        g2d.setColor(Color.WHITE);
        g2d.drawOval(myPoint.x - nodeRadius, myPoint.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

        // Draw key with Shadow Offset Method for outline
        if (p.getElement() != null && p.getElement().getKey() != null) {
            String key = p.getElement().getKey().toString();
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(key);
            int textHeight = fm.getHeight();
            int x = myPoint.x - textWidth / 2;
            int y = myPoint.y + textHeight / 4;

            // Draw black shadow/outline in 4 directions
            g2d.setColor(Color.BLACK);
            g2d.drawString(key, x - 1, y - 1);
            g2d.drawString(key, x + 1, y - 1);
            g2d.drawString(key, x - 1, y + 1);
            g2d.drawString(key, x + 1, y + 1);

            // Draw main white text
            g2d.setColor(Color.WHITE);
            g2d.drawString(key, x, y);
        }

        // Draw children
        drawTree(g2d, getLeft(p), myPoint.x, myPoint.y);
        drawTree(g2d, getRight(p), myPoint.x, myPoint.y);
    }
}
