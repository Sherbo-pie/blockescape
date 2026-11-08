package blockescape;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** Pill-shaped button used on every screen. */
public class StyledButton extends JButton {

    public enum Kind { PRIMARY, SECONDARY }

    private final Kind kind;

    public StyledButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);        // keep keyboard focus on the window
        setOpaque(false);
        setFont(Theme.font(Font.BOLD, 19));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(260, 52);
        setPreferredSize(d);
        setMaximumSize(d);
        setMinimumSize(d);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight() - 5;
        boolean hot = getModel().isRollover() && isEnabled();
        boolean down = getModel().isPressed() && isEnabled();

        Color top;
        Color bottom;
        Color text;
        if (!isEnabled()) {
            top = new Color(220, 216, 232);
            bottom = new Color(200, 196, 214);
            text = new Color(150, 145, 175);
        } else if (kind == Kind.PRIMARY) {
            top = hot ? new Color(250, 86, 130) : new Color(240, 62, 112);
            bottom = new Color(200, 30, 78);
            text = Color.WHITE;
        } else {
            top = Color.WHITE;
            bottom = hot ? new Color(232, 226, 252) : new Color(240, 236, 250);
            text = Theme.INK;
        }

        int dy = down ? 3 : 0;
        g2.setColor(new Color(50, 25, 100, 70));
        g2.fillRoundRect(2, 5, w - 4, h, h, h);
        g2.setPaint(new GradientPaint(0, dy, top, 0, h + dy, bottom));
        g2.fillRoundRect(0, dy, w, h, h, h);

        g2.setFont(getFont());
        int tx = (w - g2.getFontMetrics().stringWidth(getText())) / 2;
        int ty = dy + (h + g2.getFontMetrics().getAscent()
                - g2.getFontMetrics().getDescent()) / 2;
        g2.setColor(text);
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
