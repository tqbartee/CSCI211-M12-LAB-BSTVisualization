import com.zybooks.dsaj.searchtree.TreeMap;
import com.zybooks.dsaj.util.Entry;
import com.zybooks.dsaj.util.Position;
import java.util.Comparator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * An implementation of a sorted map using a splay tree, with Swing animation.
 */
public class SplayTreeMapAnimate<K,V> extends TreeMap<K,V> implements TreeAnimation<K,V> {

    private JFrame frame;
    private RBTreePanel<K, V> treePanel;
    private String currentOperation = "";
    private K lastDeletedKey;
    private volatile boolean isPaused = false;
    private volatile boolean isFinished = false;
    private final Object pauseLock = new Object();
    private JButton pauseButton;
    public static final int DEFAULT_ANIMATION_DELAY_MS = 2000;
    private static final int ANIMATION_DELAY_MS = DEFAULT_ANIMATION_DELAY_MS;
    private int animationDelayMs = DEFAULT_ANIMATION_DELAY_MS;

    public SplayTreeMapAnimate() { super(); }

    public SplayTreeMapAnimate(Comparator<K> comp) { super(comp); }

    /**
     * Constructs an empty map using natural ordering with specified animation delay.
     * @param animationDelayMs animation delay in milliseconds
     */
    public SplayTreeMapAnimate(int animationDelayMs) {
        super();
        this.animationDelayMs = Math.max(0, animationDelayMs);
    }

    /**
     * Constructs an empty map using the given comparator and animation delay.
     * @param comp comparator defining the order of keys in the map
     * @param animationDelayMs animation delay in milliseconds
     */
    public SplayTreeMapAnimate(Comparator<K> comp, int animationDelayMs) {
        super(comp);
        this.animationDelayMs = Math.max(0, animationDelayMs);
    }

    /**
     * Constructs an empty map using natural ordering with specified animation delay in seconds.
     * @param animationDelaySeconds animation delay in seconds
     */
    public SplayTreeMapAnimate(double animationDelaySeconds) {
        this((int) Math.round(animationDelaySeconds * 1000));
    }

    /**
     * Constructs an empty map using the given comparator and animation delay in seconds.
     * @param comp comparator defining the order of keys in the map
     * @param animationDelaySeconds animation delay in seconds
     */
    public SplayTreeMapAnimate(Comparator<K> comp, double animationDelaySeconds) {
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

    // Make these public for TreePanel
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

    // Dummy for RBTreePanel compatibility
    public boolean isRed(Position<Entry<K,V>> p) { return false; }

    /** Utility used to rebalance after a map operation. */
    private void splay(Position<Entry<K,V>> p) {
        while (!isRoot(p)) {
            Position<Entry<K,V>> parent = parent(p);
            Position<Entry<K,V>> grand = parent(parent);
            String keyStr = (isExternal(p)) ? "leaf" : p.getElement().getKey().toString();
            if (grand == null) {                                          // zig case
                setStatus("Zig rotation at key " + keyStr);
                rotate(p);
            } else if ((parent == left(grand)) == (p == left(parent))) {  // zig-zig case
                setStatus("Zig-zig rotation at key " + keyStr);
                rotate(parent);      // move PARENT upward
                rotate(p);           // then move p upward
            } else {                                                    // zig-zag case
                setStatus("Zig-zag rotation at key " + keyStr);
                rotate(p);           // move p upward
                rotate(p);           // move p upward again
            }
        }
    }

    @Override
    protected void rebalanceAccess(Position<Entry<K,V>> p) {
        showAnimationWindow("Accessing " + (isExternal(p) ? "leaf" : p.getElement().getKey()));
        if (isExternal(p)) p = parent(p);
        if (p != null) splay(p);
        setStatus("Access and splay complete");
        // hideAnimationWindow();
    }

    @Override
    protected void rebalanceInsert(Position<Entry<K,V>> p) {
        showAnimationWindow("Inserting " + p.getElement().getKey());
        splay(p);
        setStatus("Insertion and splay complete");
        // hideAnimationWindow();
    }

    @Override
    public V remove(K key) {
        lastDeletedKey = key;
        return super.remove(key);
    }

    @Override
    protected void rebalanceDelete(Position<Entry<K,V>> p) {
        showAnimationWindow("Deleting " + lastDeletedKey);
        if (!isRoot(p)) splay(parent(p));
        setStatus("Deletion and splay complete");
        // hideAnimationWindow();
    }

    private void ensureGuiInitialized() {
        if (frame == null) {
            Runnable guiCreator = () -> {
                frame = new JFrame("Splay Tree Animation");
                treePanel = new RBTreePanel(this);
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
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLocationRelativeTo(null);

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
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
                guiCreator.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(guiCreator);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void finishAnimation() {
        if (frame == null || !frame.isVisible()) return;
        isFinished = true;
        SwingUtilities.invokeLater(() -> {
            pauseButton.setText("Close");
            frame.setTitle("Splay Tree Animation - Complete");
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
            frame.setTitle("Splay Tree Animation - " + currentOperation + " (" + status + ")");
            treePanel.repaint();
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
            if (frame != null) {
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
}
