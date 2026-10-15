import com.zybooks.dsaj.util.Position;
import com.zybooks.dsaj.util.Entry;

/**
 * Interface for animatable trees to provide a way to signal the end of the animation.
 */
public interface TreeAnimation<K, V> {
    /**
     * Called when the building of the tree is complete.
     * Changes the "Pause" button to "Close" and keeps the window open.
     */
    void finishAnimation();

    /**
     * Sets the animation delay between steps in milliseconds.
     * @param delayMs delay in milliseconds
     */
    void setAnimationDelay(int delayMs);

    /**
     * Returns the animation delay between steps in milliseconds.
     * @return delay in milliseconds
     */
    int getAnimationDelay();

    /**
     * Sets the animation delay between steps in seconds.
     * @param seconds delay in seconds
     */
    default void setAnimationDelaySeconds(double seconds) {
        setAnimationDelay((int) Math.round(Math.max(0.0, seconds) * 1000));
    }

    // Methods needed by RBTreePanel
    Position<Entry<K,V>> root();
    Position<Entry<K,V>> parent(Position<Entry<K,V>> p);
    Position<Entry<K,V>> left(Position<Entry<K,V>> p);
    Position<Entry<K,V>> right(Position<Entry<K,V>> p);
    boolean isExternal(Position<Entry<K,V>> p);
    boolean isRed(Position<Entry<K,V>> p);
}
