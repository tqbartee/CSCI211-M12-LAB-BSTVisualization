import com.zybooks.dsaj.searchtree.TreeMap;
import com.zybooks.dsaj.util.Entry;
import com.zybooks.dsaj.util.Position;
import java.util.Comparator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * An implementation of a sorted map using a red-black tree, with Swing animation.
 *
 * @param <K> The key type (keys must be unique and comparable)
 * @param <V> The value type
 *
 * @author Michael T. Goodrich
 * @author Roberto Tamassia
 * @author Michael H. Goldwasser
 */
public class RBTreeMapAnimate<K,V> extends TreeMap<K,V> implements TreeAnimation<K,V> {

    private JFrame frame;
    private RBTreePanel<K, V> treePanel;
    private String currentOperation = "";
    private K lastDeletedKey;
    private volatile boolean isPaused = false;
    private volatile boolean isFinished = false;
    private final Object pauseLock = new Object();
    private JButton pauseButton;
    public static final int DEFAULT_ANIMATION_DELAY_MS = 5000; // 5 seconds
    private static final int ANIMATION_DELAY_MS = DEFAULT_ANIMATION_DELAY_MS;
    private int animationDelayMs = DEFAULT_ANIMATION_DELAY_MS;

    /** Constructs an empty map using the natural ordering of keys. */
    public RBTreeMapAnimate() {
        super();
    }

    /**
     * Constructs an empty map using the given comparator to order keys.
     * @param comp comparator defining the order of keys in the map
     */
    public RBTreeMapAnimate(Comparator<K> comp) {
        super(comp);
    }

    /**
     * Constructs an empty map using natural ordering with specified animation delay.
     * @param animationDelayMs animation delay in milliseconds
     */
    public RBTreeMapAnimate(int animationDelayMs) {
        super();
        this.animationDelayMs = Math.max(0, animationDelayMs);
    }

    /**
     * Constructs an empty map using the given comparator and animation delay.
     * @param comp comparator defining the order of keys in the map
     * @param animationDelayMs animation delay in milliseconds
     */
    public RBTreeMapAnimate(Comparator<K> comp, int animationDelayMs) {
        super(comp);
        this.animationDelayMs = Math.max(0, animationDelayMs);
    }

    /**
     * Constructs an empty map using natural ordering with specified animation delay in seconds.
     * @param animationDelaySeconds animation delay in seconds
     */
    public RBTreeMapAnimate(double animationDelaySeconds) {
        this((int) Math.round(animationDelaySeconds * 1000));
    }

    /**
     * Constructs an empty map using the given comparator and animation delay in seconds.
     * @param comp comparator defining the order of keys in the map
     * @param animationDelaySeconds animation delay in seconds
     */
    public RBTreeMapAnimate(Comparator<K> comp, double animationDelaySeconds) {
        this(comp, (int) Math.round(animationDelaySeconds * 1000));
    }

    @Override
    public void setAnimationDelay(int delayMs) {
        this.animationDelayMs = Math.max(0, delayMs);
    }

    @Override
    public int getAnimationDelay() {
        return this.animationDelayMs;
    }

    @Override
    public void setAnimationDelaySeconds(double seconds) {
        this.animationDelayMs = (int) Math.round(Math.max(0.0, seconds) * 1000);
    }

    // Make these public for RBTreePanel
    @Override
    public Position<Entry<K,V>> root() { return super.root(); }
    @Override
    public Position<Entry<K,V>> parent(Position<Entry<K,V>> p) { return super.parent(p); }
    @Override
    public Position<Entry<K,V>> left(Position<Entry<K,V>> p) { return super.left(p); }
    @Override
    public Position<Entry<K,V>> right(Position<Entry<K,V>> p) { return super.right(p); }
    @Override
    public boolean isExternal(Position<Entry<K,V>> p) { return super.isExternal(p); }

    private void ensureGuiInitialized() {
        if (frame == null) {
            Runnable guiCreator = () -> {
                frame = new JFrame("Red-Black Tree Animation");
                treePanel = new RBTreePanel<>(this);
                JScrollPane scrollPane = new JScrollPane(treePanel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                frame.setLayout(new BorderLayout());
                frame.add(scrollPane, BorderLayout.CENTER);

                JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                pauseButton = new JButton("Pause");
                pauseButton.addActionListener(e -> {
                    if (isFinished) {
                        frame.dispose();
                        return;
                    }
                    isPaused = !isPaused;
                    pauseButton.setText(isPaused ? "Continue" : "Pause");
                    if (!isPaused) {
                        synchronized (pauseLock) {
                            pauseLock.notifyAll();
                        }
                    }
                });
                controlPanel.add(pauseButton);
                frame.add(controlPanel, BorderLayout.SOUTH);

                frame.pack();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose, not exit
                frame.setLocationRelativeTo(null); // Center the window

                // Add a WindowListener to handle closing the window programmatically
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        // Ensure animation doesn't stay paused if window is closed
                        isPaused = false;
                        synchronized (pauseLock) {
                            pauseLock.notifyAll();
                        }
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        isPaused = false;
                        synchronized (pauseLock) {
                            pauseLock.notifyAll();
                        }
                    }
                });
            };

            if (SwingUtilities.isEventDispatchThread()) {
                guiCreator.run(); // Execute directly if already on EDT
            } else {
                try {
                    SwingUtilities.invokeAndWait(guiCreator); // Use invokeAndWait if not on EDT
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Error initializing GUI on EDT: " + e.getMessage());
                }
            }
        }
    }

    public void finishAnimation() {
        if (frame == null || !frame.isVisible()) return;
        isFinished = true;
        SwingUtilities.invokeLater(() -> {
            pauseButton.setText("Close");
            frame.setTitle("Red-Black Tree Animation - Complete");
        });

        synchronized (pauseLock) {
            while (frame.isVisible()) {
                try {
                    pauseLock.wait(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void setStatus(String status) {
        ensureGuiInitialized();
        SwingUtilities.invokeLater(() -> {
            frame.setTitle("Red-Black Tree Animation - " + currentOperation + " (" + status + ")");
            treePanel.setTreeMap(this);
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);
        });
        delay();
    }

    private void showAnimationWindow(String title) {
        currentOperation = title;
        setStatus("Starting");
    }

    private void hideAnimationWindow() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) { // Only hide if frame exists
                frame.setVisible(false);
            }
        });
    }

    private void delay() {
        long remaining = animationDelayMs;
        synchronized (pauseLock) {
            while (remaining > 0 || isPaused) {
                if (isPaused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    long start = System.currentTimeMillis();
                    try {
                        pauseLock.wait(remaining);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    long elapsed = System.currentTimeMillis() - start;
                    remaining -= elapsed;
                }
            }
        }
    }

    // we use the inherited aux field with convention that 0=black and 1=red
    // (note that new leaves will be black by default, as aux=0)
    public boolean isBlack(Position<Entry<K,V>> p) { return tree.getAux(p)==0;}

    public boolean isRed(Position<Entry<K,V>> p) { return tree.getAux(p)==1; }

    private void makeBlack(Position<Entry<K,V>> p) {
        tree.setAux(p, 0);
        SwingUtilities.invokeLater(() -> treePanel.repaint());
        // setStatus is handled by caller to avoid too many delays or inconsistent titles
    }

    private void makeRed(Position<Entry<K,V>> p) {
        tree.setAux(p, 1);
        SwingUtilities.invokeLater(() -> treePanel.repaint());
        // setStatus is handled by caller
    }

    private void setColor(Position<Entry<K,V>> p, boolean toRed) {
        tree.setAux(p, toRed ? 1 : 0);
        SwingUtilities.invokeLater(() -> treePanel.repaint());
    }

    /** Overrides the TreeMap rebalancing hook that is called after an insertion. */
    @Override
    protected void rebalanceInsert(Position<Entry<K,V>> p) {
        if (!isRoot(p)) tree.setAux(p, 1); // Logically, a new node is red from the start
        showAnimationWindow("Inserting " + p.getElement().getKey());
        if (!isRoot(p)) {
            resolveRed(p);                // but this may cause a double-red problem
        }
        // hideAnimationWindow();
    }

    /** Remedies potential double-red violation above red position p. */
    private void resolveRed(Position<Entry<K,V>> p) {
        Position<Entry<K,V>> parent,uncle,middle,grand;  // used in case analysis
        parent = parent(p);
        if (isRed(parent)) {                             // double-red problem exists
            setStatus("Double-red violation detected");
            uncle = sibling(parent);
            if (isExternal(uncle) || isBlack(uncle)) {   // Case 1: misshapen 4-node
                setStatus("Resolving Case 1: Trinode restructuring");
                middle = restructure(p);                 // do trinode restructuring
                setStatus("Restructuring complete, recoloring nodes");
                makeBlack(middle);
                makeRed(left(middle));
                makeRed(right(middle));
                setStatus("Case 1 resolved");
            } else {                                     // Case 2: overfull 5-node
                setStatus("Resolving Case 2: Recoloring parent and uncle");
                makeBlack(parent);                       // perform recoloring
                makeBlack(uncle);
                grand = parent(parent);
                if (!isRoot(grand)) {
                    setStatus("Recoloring grandparent red");
                    makeRed(grand);                      // grandparent becomes red
                    setStatus("Resolving double-red at grandparent");
                    resolveRed(grand);                   // recur at red grandparent
                } else {
                    setStatus("Grandparent is root, stays black");
                }
            }
        }
    }

    @Override
    public V remove(K key) {
        lastDeletedKey = key;
        return super.remove(key);
    }

    /** Overrides the TreeMap rebalancing hook that is called after a deletion. */
    @Override
    protected void rebalanceDelete(Position<Entry<K,V>> p) {
        showAnimationWindow("Deleting " + lastDeletedKey);
        if (isInternal(p) && isRed(p)) {                                    // deleted parent was black
            setStatus("Restoring black depth (coloring replacement black)");
            makeBlack(p);                                // so this restores black depth
        } else if (!isRoot(p)) {
            Position<Entry<K,V>> sib = sibling(p);
            if (isInternal(sib) && (isBlack(sib) || isInternal(left(sib)))) {
                setStatus("Double-black violation detected");
                remedyDoubleBlack(p);                    // sib's subtree has nonzero black height
            }
        }
        // hideAnimationWindow();
    }

    /** Remedies a presumed double-black violation at the given (nonroot) position. */
    private void remedyDoubleBlack(Position<Entry<K,V>> p) {
        Position<Entry<K,V>> z = parent(p);
        Position<Entry<K,V>> y = sibling(p);
        if (isBlack(y)) {
            if (isRed(left(y)) || isRed(right(y))) {     // Case 1: trinode restructuring
                setStatus("Resolving Case 1: Trinode restructuring");
                Position<Entry<K,V>> x = (isRed(left(y)) ? left(y) : right(y));
                Position<Entry<K,V>> middle = restructure(x);
                setStatus("Restructuring complete, recoloring nodes");
                setColor(middle, isRed(z));              // root of restructured subtree gets z's old color
                makeBlack(left(middle));
                makeBlack(right(middle));
                setStatus("Case 1 resolved");
            } else {                                     // Case 2: recoloring
                setStatus("Resolving Case 2: Recoloring sibling red");
                makeRed(y);
                if (isRed(z)) {
                    setStatus("Recoloring parent black");
                    makeBlack(z);                        // problem is resolved
                    setStatus("Case 2 resolved");
                } else if (!isRoot(z)) {
                    setStatus("Propagating double-black to parent");
                    remedyDoubleBlack(z);                // propagate the problem
                }
            }
        } else {                                         // Case 3: reorient 3-node
            setStatus("Resolving Case 3: Rotating sibling");
            rotate(y);
            setStatus("Recoloring nodes after rotation");
            makeBlack(y);
            makeRed(z);
            setStatus("Restarting rebalance process after reorientation");
            remedyDoubleBlack(p);                        // restart the process at p
        }
    }

    /** Ensure that current tree structure is valid RB tree (for debugging only)*/
    private boolean sanityCheck() {
        if (sanityRecurse(root()) == -1) {
            System.out.println("VIOLATION of RB tree properties");
            dump();
            return false;
        } else
            return true;
    }

    /** Returns black depth of subtree, if valid, or -1 if invalid. */
    private int sanityRecurse(Position<Entry<K,V>> p) {
        if (isExternal(p)) {
            if (isRed(p)) return -1;                     // invalid; should be black
            else return 0;                               // valid, with black-depth 0
        } else {
            if (isRoot(p) && isRed(p)) return -1;        // root must be black
            Position<Entry<K,V>> left = left(p);
            Position<Entry<K,V>> right = right(p);
            if (isRed(p) && (isRed(left) || isRed(right))) return -1;   // cannot have double red

            int a = sanityRecurse(left);
            if (a == -1) return -1;
            int b = sanityRecurse(right);
            if (a != b) return -1;                       // two subtrees must have identical black depth

            return a + (isRed(p) ? 0 : 1);               // our black depth might be one greater
        }
    }
}
