package blockescape;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main window. Holds the Home, Level Select and Gameplay screens in a
 * CardLayout and switches between them. Start the app from GuiMain.
 */
public class GameWindow extends JFrame implements ScreenNavigator {

    private static final String HOME = "home";
    private static final String LEVELS = "levels";
    private static final String GAME = "game";

    private final GameController controller;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final HomeScreen home;
    private final LevelSelectScreen levelSelect;
    private final GamePanel game;

    public GameWindow(GameController controller) {
        super("Block Escape");
        this.controller = controller;

        home = new HomeScreen(controller, this);
        levelSelect = new LevelSelectScreen(controller, this);
        game = new GamePanel(controller, this);

        root.add(home, HOME);
        root.add(levelSelect, LEVELS);
        root.add(game, GAME);
        root.setPreferredSize(new Dimension(960, 680));
        add(root);

        new InputHandler(controller, game::isInputEnabled,
                game::togglePause).install(getRootPane());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);
        showHome();
    }

    @Override
    public void showHome() {
        home.refresh();
        cards.show(root, HOME);
    }

    @Override
    public void showLevels() {
        levelSelect.repaint();
        cards.show(root, LEVELS);
    }

    @Override
    public void startLevel(int levelNumber) {
        if (controller.startLevel(levelNumber)) {
            game.onEnter();
            cards.show(root, GAME);
        }
    }

    @Override
    public void continueGame() {
        startLevel(controller.getHighestUnlocked());
    }

    @Override
    public void quit() {
        dispose();
        System.exit(0);
    }
}
