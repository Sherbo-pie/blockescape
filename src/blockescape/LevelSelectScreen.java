package blockescape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import javax.swing.JPanel;

/** Grid of level cards with locked / cleared states. */
public class LevelSelectScreen extends BackgroundPanel {

    private static final int GAP = 26;

    private final GameController controller;
    private final ScreenNavigator nav;
    private int hover = -1;

    public LevelSelectScreen(GameController controller, ScreenNavigator nav) {
        this.controller = controller;
        this.nav = nav;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 20));
        top.setOpaque(false);
        StyledButton back =
                new StyledButton("Back", StyledButton.Kind.SECONDARY);
        back.setPreferredSize(new Dimension(120, 46));
        back.addActionListener(e -> nav.showHome());
        top.add(back);
        add(top, BorderLayout.NORTH);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = levelAt(e.getPoint());
                if (i != hover) {
                    hover = i;
                    setCursor(Cursor.getPredefinedCursor(
                            i > 0 && controller.isUnlocked(i)
                                    ? Cursor.HAND_CURSOR
                                    : Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = -1;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int i = levelAt(e.getPoint());
                if (i > 0 && controller.isUnlocked(i)) {
                    nav.startLevel(i);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private int cardSize() {
        int perRow = Math.min(controller.getTotalLevels(), 5);
        int fit = (getWidth() - 120 - (perRow - 1) * GAP) / perRow;
        return Math.max(70, Math.min(140, fit));
    }

    private Rectangle cardRect(int level) {
        int n = controller.getTotalLevels();
        int perRow = Math.min(n, 5);
        int rows = (n + perRow - 1) / perRow;
        int size = cardSize();
        int totalW = perRow * size + (perRow - 1) * GAP;
        int totalH = rows * size + (rows - 1) * GAP;
        int x0 = (getWidth() - totalW) / 2;
        int y0 = (getHeight() - totalH) / 2 + 30;
        int row = (level - 1) / perRow;
        int col = (level - 1) % perRow;
        return new Rectangle(x0 + col * (size + GAP),
                y0 + row * (size + GAP), size, size);
    }

    private int levelAt(java.awt.Point pt) {
        for (int i = 1; i <= controller.getTotalLevels(); i++) {
            if (cardRect(i).contains(pt)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void paintContent(Graphics2D g) {
        int w = getWidth();
        Theme.shadowText(g, "Select Level", w / 2, 78,
                Theme.font(Font.BOLD, 46), Color.WHITE);
        Theme.shadowText(g, "Cleared " + controller.getClearedCount() + " / "
                + controller.getTotalLevels(), w / 2, 110,
                Theme.font(Font.PLAIN, 18), new Color(255, 255, 255, 220));

        for (int i = 1; i <= controller.getTotalLevels(); i++) {
            drawCard(g, i, cardRect(i));
        }
    }

    private void drawCard(Graphics2D g, int level, Rectangle r) {
        boolean unlocked = controller.isUnlocked(level);
        boolean cleared = controller.getBestMoves(level) > 0;
        boolean hot = hover == level && unlocked;
        int lift = hot ? -5 : 0;
        int arc = 28;

        g.setColor(new Color(40, 20, 90, 60));
        g.fillRoundRect(r.x, r.y + 8, r.width, r.height, arc, arc);
        g.setColor(unlocked ? new Color(255, 255, 255, 245)
                : new Color(255, 255, 255, 110));
        g.fillRoundRect(r.x, r.y + lift, r.width, r.height, arc, arc);
        if (hot) {
            g.setColor(Theme.CRIMSON);
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(r.x, r.y + lift, r.width, r.height, arc, arc);
        }

        int cx = r.x + r.width / 2;
        int cy = r.y + lift + r.height / 2;

        if (!unlocked) {
            drawPadlock(g, cx, cy);
            return;
        }

        g.setFont(Theme.font(Font.BOLD, r.width / 2));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.INK);
        g.drawString(String.valueOf(level),
                cx - fm.stringWidth(String.valueOf(level)) / 2,
                cy + fm.getAscent() / 3 - (cleared ? 6 : 0));

        if (cleared) {
            String best = "Best " + controller.getBestMoves(level);
            g.setFont(Theme.font(Font.PLAIN, 15));
            g.setColor(new Color(110, 96, 160));
            g.drawString(best,
                    cx - g.getFontMetrics().stringWidth(best) / 2,
                    r.y + lift + r.height - 18);

            int bx = r.x + r.width - 24;
            int by = r.y + lift + 24;
            g.setColor(Theme.GREEN);
            g.fillOval(bx - 13, by - 13, 26, 26);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.drawPolyline(new int[] {bx - 6, bx - 1, bx + 7},
                    new int[] {by, by + 6, by - 5}, 3);
        }
    }

    private void drawPadlock(Graphics2D g, int cx, int cy) {
        Color c = new Color(120, 106, 170, 200);
        g.setColor(c);
        g.setStroke(new BasicStroke(5f));
        g.draw(new Arc2D.Double(cx - 13, cy - 30, 26, 30, 0, 180, Arc2D.OPEN));
        g.fillRoundRect(cx - 21, cy - 8, 42, 32, 10, 10);
        g.setColor(new Color(255, 255, 255, 220));
        g.fillOval(cx - 4, cy + 3, 8, 8);
    }
}
