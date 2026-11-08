package blockescape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.Timer;

/**
 * Gameplay screen: isometric board, HUD buttons, and the pause / win /
 * loss overlays. It only reads state from GameController; it never decides
 * whether a move is legal.
 */
public class GamePanel extends BackgroundPanel {

    private enum Overlay { NONE, PAUSE, RESULT }

    private static final int BTN = 58;
    private static final int MARGIN = 22;

    private final GameController ctl;
    private final ScreenNavigator nav;
    private final OverlayPanel overlay = new OverlayPanel();
    private Overlay overlayKind = Overlay.NONE;

    // short delay so the player sees the fall / goal before the popup
    private final Timer resultTimer;

    private final Ellipse2D.Double menuBtn = new Ellipse2D.Double();
    private final Ellipse2D.Double undoBtn = new Ellipse2D.Double();
    private final Ellipse2D.Double restartBtn = new Ellipse2D.Double();
    private Ellipse2D.Double hovered;

    private String toast;
    private long toastUntil;

    public GamePanel(GameController controller, ScreenNavigator nav) {
        this.ctl = controller;
        this.nav = nav;
        resultTimer = new Timer(550, e -> {
            if (ctl.getStatus() != GameRules.Status.PLAYING
                    && overlayKind == Overlay.NONE) {
                showResult();
            }
        });
        resultTimer.setRepeats(false);

        setLayout(new BorderLayout());
        overlay.setVisible(false);
        add(overlay, BorderLayout.CENTER);

        controller.addChangeListener(this::onGameChanged);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                layoutHud();
                Ellipse2D.Double now = hudButtonAt(e.getPoint());
                if (now != hovered) {
                    hovered = now;
                    setCursor(Cursor.getPredefinedCursor(now == null
                            ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                layoutHud();
                Ellipse2D.Double hit = hudButtonAt(e.getPoint());
                if (hit == menuBtn) {
                    showPause();
                
                } else if (hit == undoBtn) {
                    ctl.undo();
                } else if (hit == restartBtn) {
                    ctl.restartLevel();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // ---------- called by GameWindow / InputHandler ----------

    /** Reset transient UI whenever the screen is entered. */
    public void onEnter() {
        resultTimer.stop();
        hideOverlay();
        toast = null;
        repaint();
    }

    public boolean isInputEnabled() {
        return isShowing() && overlayKind != Overlay.PAUSE;
    }

    public void togglePause() {
        if (!isShowing()) {
            return;
        }
        if (overlayKind == Overlay.PAUSE) {
            hideOverlay();
        } else if (overlayKind == Overlay.NONE) {
            showPause();
        }
    }

    // ---------- overlays ----------

    private void onGameChanged() {
        if (ctl.getStatus() == GameRules.Status.PLAYING) {
            resultTimer.stop();
            if (overlayKind == Overlay.RESULT) {
                hideOverlay();
            }
        } else if (overlayKind == Overlay.NONE) {
            resultTimer.restart();
        }
        repaint();
    }

    private StyledButton option(String text, boolean primary, Runnable action) {
        StyledButton b = new StyledButton(text, primary
                ? StyledButton.Kind.PRIMARY : StyledButton.Kind.SECONDARY);
        b.addActionListener(e -> {
            hideOverlay();
            action.run();
        });
        return b;
    }

    private void showPause() {
        if (overlayKind != Overlay.NONE) {
            return;
        }
        overlayKind = Overlay.PAUSE;
        overlay.display("Paused", "Level " + ctl.getLevelNumber(), Theme.INK,
                option("Resume", true, () -> { }),
                option("Restart", false, ctl::restartLevel),
                option("Level Select", false, nav::showLevels),
                option("Home", false, nav::showHome));
        overlay.setVisible(true);
        revalidate();
        repaint();
    }

    private void showResult() {
        overlayKind = Overlay.RESULT;
        int n = ctl.getLevelNumber();
        String stats = "Moves: " + ctl.getMoves();

        if (ctl.getStatus() == GameRules.Status.FALLEN) {
            overlay.display("You Fell!", stats, Theme.RED,
                    option("Undo", true, ctl::undo),
                    option("Restart", false, ctl::restartLevel),
                    option("Level Select", false, nav::showLevels));
        } else {
            if (ctl.getBestMoves(n) > 0) {
                stats += "      Best: " + ctl.getBestMoves(n);
            }
            if (ctl.hasNextLevel()) {
                overlay.display("Level Cleared!", stats, Theme.GREEN,
                        option("Next Level", true, ctl::nextLevel),
                        option("Replay", false, ctl::restartLevel),
                        option("Level Select", false, nav::showLevels));
            } else {
                overlay.display("All Levels Cleared!", stats, Theme.GREEN,
                        option("Level Select", true, nav::showLevels),
                        option("Replay", false, ctl::restartLevel),
                        option("Home", false, nav::showHome));
            }
        }
        overlay.setVisible(true);
        revalidate();
        repaint();
    }

    private void hideOverlay() {
        overlayKind = Overlay.NONE;
        overlay.setVisible(false);
        repaint();
    }

    // ---------- HUD geometry ----------

    private void layoutHud() {
        int w = getWidth();
        int h = getHeight();
        menuBtn.setFrame(MARGIN, MARGIN, BTN, BTN);
        restartBtn.setFrame(w - MARGIN - BTN, h - MARGIN - BTN, BTN, BTN);
        undoBtn.setFrame(w - MARGIN - 2 * BTN - 14, h - MARGIN - BTN, BTN, BTN);
    }

    private Ellipse2D.Double hudButtonAt(Point p) {
        for (Ellipse2D.Double b : new Ellipse2D.Double[] {
                menuBtn, undoBtn, restartBtn}) {
            if (b.contains(p)) {
                return b;
            }
        }
        return null;
    }

    // ---------- painting ----------

    @Override
    protected void paintContent(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        layoutHud();

        IsoRenderer.render(g, ctl, w, h, 100, 100);

        // title
        Theme.shadowText(g, "Level " + ctl.getLevelNumber(), w / 2, 62,
                Theme.font(Font.BOLD, 38), Color.WHITE);

        // move counter (top right)
        String moves = String.valueOf(ctl.getMoves());
        g.setFont(Theme.font(Font.BOLD, 38));
        int tw = g.getFontMetrics().stringWidth(moves);
        int right = w - MARGIN - 8;
        Theme.shadowText(g, moves, right - tw / 2, 62,
                Theme.font(Font.BOLD, 38), Color.WHITE);
        int ix = right - tw - 48;
        g.setPaint(new GradientPaint(ix, 30, new Color(242, 64, 114),
                ix, 62, new Color(190, 28, 78)));
        g.fillRoundRect(ix, 32, 32, 32, 6, 6);

        drawButton(g, menuBtn, true);
    
        drawButton(g, undoBtn, ctl.canUndo());
        drawButton(g, restartBtn, true);

        Color icon = Theme.INK;
        drawMenuIcon(g, menuBtn, icon);
    
        drawArcArrow(g, undoBtn, ctl.canUndo()
                ? icon : new Color(170, 160, 200), 0, 190);
        drawArcArrow(g, restartBtn, icon, 110, -300);

        if (toast != null) {
            if (System.currentTimeMillis() > toastUntil) {
                toast = null;
            } else {
                g.setFont(Theme.font(Font.BOLD, 17));
                int tw2 = g.getFontMetrics().stringWidth(toast) + 40;
                int tx = (w - tw2) / 2;
                int ty = h - 92;
                g.setColor(new Color(60, 40, 120, 210));
                g.fillRoundRect(tx, ty, tw2, 38, 38, 38);
                g.setColor(Color.WHITE);
                g.drawString(toast, tx + 20, ty + 25);
            }
        }
    }

    private void drawButton(Graphics2D g, Ellipse2D.Double b, boolean enabled) {
        boolean hot = (b == hovered) && enabled;
        g.setColor(new Color(50, 25, 100, 60));
        g.fillOval((int) b.x, (int) b.y + 5, BTN, BTN);
        g.setColor(hot ? Color.WHITE : new Color(255, 255, 255, 215));
        g.fillOval((int) b.x, (int) b.y, BTN, BTN);
        g.setColor(new Color(255, 255, 255, 240));
        g.setStroke(new BasicStroke(2f));
        g.drawOval((int) b.x, (int) b.y, BTN, BTN);
    }

    private void drawMenuIcon(Graphics2D g, Ellipse2D.Double b, Color c) {
        g.setColor(c);
        int x = (int) b.x + BTN / 2 - 12;
        int y = (int) b.y + BTN / 2;
        for (int i = -1; i <= 1; i++) {
            g.fillRoundRect(x, y + i * 9 - 2, 24, 4, 4, 4);
        }
    }

    /*private void drawBulbIcon(Graphics2D g, Ellipse2D.Double b, Color c) {
        int cx = (int) b.x + BTN / 2;
        int cy = (int) b.y + BTN / 2;
        g.setColor(c);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawOval(cx - 10, cy - 15, 20, 20);
        g.drawLine(cx - 5, cy + 10, cx + 5, cy + 10);
        g.drawLine(cx - 3, cy + 15, cx + 3, cy + 15);
    }*/

    /** Circular arrow; extent > 0 is anticlockwise, < 0 clockwise. */
    private void drawArcArrow(Graphics2D g, Ellipse2D.Double b, Color c,
                              double start, double extent) {
        double cx = b.x + BTN / 2.0;
        double cy = b.y + BTN / 2.0;
        double r = 11;
        g.setColor(c);
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, start, extent,
                Arc2D.OPEN));

        double endDeg = Math.toRadians(start + extent);
        double ex = cx + r * Math.cos(endDeg);
        double ey = cy - r * Math.sin(endDeg);
        double sign = Math.signum(extent);
        double dx = sign * -Math.sin(endDeg);
        double dy = sign * -Math.cos(endDeg);
        double px = -dy;
        double py = dx;
        double s = 7;
        Path2D.Double head = new Path2D.Double();
        head.moveTo(ex + dx * s, ey + dy * s);
        head.lineTo(ex + px * s * 0.8, ey + py * s * 0.8);
        head.lineTo(ex - px * s * 0.8, ey - py * s * 0.8);
        head.closePath();
        g.fill(head);
    }
}
