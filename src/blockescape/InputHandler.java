package blockescape;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.BooleanSupplier;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Turns key presses into controller calls. Uses key bindings (not
 * KeyListener) so keys keep working after a button has been clicked.
 *
 * Arrows / WASD = move, U or Z = undo, R = restart, N or Enter = next level,
 * Esc = pause. Game keys only act while the gameplay screen accepts input.
 */
public class InputHandler {

    private final GameController controller;
    private final BooleanSupplier gameKeysEnabled;
    private final Runnable onEscape;

    public InputHandler(GameController controller,
                        BooleanSupplier gameKeysEnabled, Runnable onEscape) {
        this.controller = controller;
        this.gameKeysEnabled = gameKeysEnabled;
        this.onEscape = onEscape;
    }

    public void install(JComponent target) {
        bindMove(target, Block.Direction.UP,    KeyEvent.VK_UP,    KeyEvent.VK_W);
        bindMove(target, Block.Direction.DOWN,  KeyEvent.VK_DOWN,  KeyEvent.VK_S);
        bindMove(target, Block.Direction.LEFT,  KeyEvent.VK_LEFT,  KeyEvent.VK_A);
        bindMove(target, Block.Direction.RIGHT, KeyEvent.VK_RIGHT, KeyEvent.VK_D);

        bind(target, "undo", () -> {
            if (gameKeysEnabled.getAsBoolean()) {
                controller.undo();
            }
        }, KeyEvent.VK_U, KeyEvent.VK_Z);

        bind(target, "restart", () -> {
            if (gameKeysEnabled.getAsBoolean()) {
                controller.restartLevel();
            }
        }, KeyEvent.VK_R);

        bind(target, "next", () -> {
            if (gameKeysEnabled.getAsBoolean()) {
                controller.nextLevel();
            }
        }, KeyEvent.VK_N, KeyEvent.VK_ENTER);

        bind(target, "pause", onEscape, KeyEvent.VK_ESCAPE);
    }

    private void bindMove(JComponent target, Block.Direction dir, int... keys) {
        bind(target, "move" + dir, () -> {
            if (gameKeysEnabled.getAsBoolean()) {
                controller.handleMove(dir);
            }
        }, keys);
    }

    private void bind(JComponent target, String name,
                      Runnable action, int... keys) {
        for (int key : keys) {
            String id = name + "-" + key;
            target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key, 0), id);
            target.getActionMap().put(id, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }
    }
}
