package blockescape;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dimmed full-screen layer with a centred card: used for the pause menu
 * and the win / loss screens. It swallows mouse clicks so nothing behind
 * it can be pressed.
 */
public class OverlayPanel extends JPanel {

    private final JLabel title = new JLabel("", JLabel.CENTER);
    private final JLabel subtitle = new JLabel("", JLabel.CENTER);
    private final JPanel buttons = new JPanel();

    public OverlayPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(30, 52, 32, 52));

        title.setFont(Theme.font(Font.BOLD, 38));
        title.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setFont(Theme.font(Font.PLAIN, 18));
        subtitle.setForeground(Theme.INK);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(buttons);
        add(card);

        MouseAdapter swallow = new MouseAdapter() { };
        addMouseListener(swallow);
        addMouseMotionListener(swallow);
    }

    public void display(String titleText, String subtitleText, Color accent,
                        JButton... options) {
        title.setText(titleText);
        title.setForeground(accent);
        subtitle.setText(subtitleText);

        buttons.removeAll();
        for (int i = 0; i < options.length; i++) {
            options[i].setAlignmentX(CENTER_ALIGNMENT);
            buttons.add(options[i]);
            if (i < options.length - 1) {
                buttons.add(Box.createVerticalStrut(12));
            }
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(30, 16, 70, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private static final class Card extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(20, 8, 60, 70));
            g2.fillRoundRect(0, 10, getWidth(), getHeight() - 6, 40, 40);
            g2.setColor(new Color(255, 255, 255, 244));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 8, 40, 40);
            g2.dispose();
        }
    }
}
