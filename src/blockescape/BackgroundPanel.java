package blockescape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Base class for every screen: paints the animated sky, then lets the
 * subclass draw its own content. The animation timer only runs while the
 * screen is visible.
 */
public abstract class BackgroundPanel extends JPanel {

    private final long startTime = System.nanoTime();
    private final Timer animation = new Timer(33, e -> repaint());

    protected BackgroundPanel() {
        setOpaque(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    animation.start();
                } else {
                    animation.stop();
                }
            }
        });
    }

    protected double seconds() {
        return (System.nanoTime() - startTime) / 1e9;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Theme.paintBackground(g2, getWidth(), getHeight(), seconds());
        paintContent(g2);
        g2.dispose();
    }

    /** Draw this screen's own artwork on top of the sky. */
    protected void paintContent(Graphics2D g) { }
}
