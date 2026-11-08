package blockescape;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Title screen: Play / Select Level / How to Play / Quit. */
public class HomeScreen extends BackgroundPanel {

    private final GameController controller;
    private final StyledButton playButton =
            new StyledButton("Play", StyledButton.Kind.PRIMARY);

    public HomeScreen(GameController controller, ScreenNavigator nav) {
        this.controller = controller;
        setLayout(new BorderLayout());

        StyledButton levels =
                new StyledButton("Select Level", StyledButton.Kind.SECONDARY);
        StyledButton help =
                new StyledButton("How to Play", StyledButton.Kind.SECONDARY);
        StyledButton quit =
                new StyledButton("Quit", StyledButton.Kind.SECONDARY);

        playButton.addActionListener(e -> nav.continueGame());
        levels.addActionListener(e -> nav.showLevels());
        help.addActionListener(e -> showHelp());
        quit.addActionListener(e -> nav.quit());

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(0, 0, 36, 0));
        StyledButton[] all = {playButton, levels, help, quit};
        for (int i = 0; i < all.length; i++) {
            all[i].setAlignmentX(CENTER_ALIGNMENT);
            column.add(all[i]);
            if (i < all.length - 1) {
                column.add(Box.createVerticalStrut(12));
            }
        }
        add(column, BorderLayout.SOUTH);
        refresh();
    }

    /** Call whenever this screen is shown. */
    public void refresh() {
        boolean started = controller.getClearedCount() > 0;
        playButton.setText(started
                ? "Continue  (Level " + controller.getHighestUnlocked() + ")"
                : "Play");
        repaint();
    }

    private void showHelp() {
        String text = "<html><body style='width:330px'>"
                + "<b>Goal:</b> roll the block until it stands upright on the "
                + "glowing green goal tile.<br><br>"
                + "<b>Controls:</b> arrow keys or WASD to roll.<br>"
                + "U / Z = undo, R = restart, Esc = pause.<br><br>"
                + "<b>Tips:</b> the block falls if any part of it hangs over "
                + "empty space or a dark hole. Step on a blue switch to "
                + "build an amber bridge tile."
                + "</body></html>";
        JOptionPane.showMessageDialog(this, text, "How to Play",
                JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    protected void paintContent(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();

        Theme.shadowText(g, "BLOCK ESCAPE", w / 2, (int) (h * 0.17),
                Theme.font(Font.BOLD, Math.max(40, Math.min(70, w / 14))),
                Color.WHITE);
        Theme.shadowText(g, "Roll the block to the glowing goal", w / 2,
                (int) (h * 0.17) + 36, Theme.font(Font.PLAIN, 19),
                new Color(255, 255, 255, 220));

        // little isometric platform with a standing block
        IsoRenderer.Projection p =
                new IsoRenderer.Projection(84, w / 2.0, h * 0.35);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                IsoRenderer.drawNormalTile(g, p, r, c);
            }
        }
        double bob = Math.sin(seconds() * 2.0) * 3;
        IsoRenderer.Projection bobbing = new IsoRenderer.Projection(
                p.tw, p.cx0, p.cy0 + bob);
        IsoRenderer.drawBlockShape(g, bobbing, 1, 1, 1, 1, 2.0);
    }
}
