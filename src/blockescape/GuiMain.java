package blockescape;

import javax.swing.SwingUtilities;

/**
 * Application entry point for the graphical game.
 * Creates the controller (game logic) and the window (GUI), then shows it.
 */
public final class GuiMain {

    private GuiMain() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            GameWindow window = new GameWindow(controller);
            window.setVisible(true);
        });
    }
}
