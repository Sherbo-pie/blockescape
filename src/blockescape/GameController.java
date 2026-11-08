package blockescape;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY controller written by Person 3 so the GUI can run.
 * Person 6 owns the real one; the GUI only calls the public methods below,
 * so swapping implementations should be painless.
 */
public class GameController {

    /** Everything needed to rewind one move. */
    private static final class Snapshot {
        final Block block;
        final int moves;
        final List<int[]> switches;

        Snapshot(Block block, int moves, List<int[]> switches) {
            this.block = block;
            this.moves = moves;
            this.switches = switches;
        }
    }

    private final LevelManager levels = new LevelManager();
    private final GameRules rules = new GameRules();
    private final MoveHistory<Snapshot> history = new MoveHistory<>();
    private final MoveCounter counter = new MoveCounter();
    private final List<int[]> activatedSwitches = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    // In-memory progress (Person 5's SaveManager can persist this later).
    private final int[] bestMoves;      // index = level number, 0 = not cleared
    private int highestUnlocked = 1;

    private Level level;
    private Block block;
    private GameRules.Status status;
    private boolean[][] initiallyVoid;  // lets the GUI tint bridge tiles

    public GameController() {
        bestMoves = new int[levels.getTotalLevels() + 1];
        startLevel(1);
    }

    // ---------- actions the GUI calls ----------

    public void startNewGame() {
        startLevel(1);
    }

    /** Loads a level if it is unlocked. */
    public boolean startLevel(int levelNumber) {
        if (!isUnlocked(levelNumber)) {
            return false;
        }
        setUpLevel(levels.loadLevel(levelNumber));
        return true;
    }

    public void restartLevel() {
        setUpLevel(levels.restartLevel());
    }

    public boolean nextLevel() {
        if (status != GameRules.Status.WON || !levels.hasNextLevel()) {
            return false;
        }
        setUpLevel(levels.nextLevel());
        return true;
    }

    public boolean handleMove(Block.Direction direction) {
        if (status != GameRules.Status.PLAYING) {
            return false;
        }

        Snapshot before = snapshot();
        GameRules.MoveResult result =
                rules.move(level.getBoard(), block, direction);

        history.save(before);
        block = result.getBlock();
        status = result.getStatus();

        // Only moves that keep the block on the board count.
        if (status != GameRules.Status.FALLEN) {
            counter.recordMove();
            triggerSwitches();
        }
        if (status == GameRules.Status.WON) {
            recordWin();
        }

        fireChanged();
        return true;
    }

    public boolean undo() {
        if (!history.canUndo()) {
            return false;
        }

        Snapshot s = history.undo();

        // Rebuild the board, then replay the switches that were active.
        level = levels.restartLevel();
        activatedSwitches.clear();
        for (int[] sw : s.switches) {
            levels.activateSwitch(sw[0], sw[1]);
            activatedSwitches.add(sw);
        }

        block = s.block;
        counter.reset();
        for (int i = 0; i < s.moves; i++) {
            counter.recordMove();
        }
        status = rules.evaluate(level.getBoard(), block);

        fireChanged();
        return true;
    }

    /**
     * Hook for Person 5's HintSolver. Returns null until it is connected.
     * The GUI already shows a hint when this returns a direction.
     */
    public Block.Direction getHint() {
        return null;
    }

    // ---------- read-only state for the GUI ----------

    public Board getBoard()              { return level.getBoard(); }
    public Block getBlock()              { return block; }
    public GameRules.Status getStatus()  { return status; }
    public int getMoves()                { return counter.getMoves(); }
    public int getLevelNumber()          { return levels.getCurrentLevelNumber(); }
    public int getTotalLevels()          { return levels.getTotalLevels(); }
    public boolean canUndo()             { return history.canUndo(); }
    public boolean hasNextLevel()        { return levels.hasNextLevel(); }

    public boolean canGoNext() {
        return status == GameRules.Status.WON && levels.hasNextLevel();
    }

    public boolean isGameComplete() {
        return status == GameRules.Status.WON && !levels.hasNextLevel();
    }

    public boolean isUnlocked(int levelNumber) {
        return levelNumber >= 1
                && levelNumber <= levels.getTotalLevels()
                && levelNumber <= highestUnlocked;
    }

    public int getHighestUnlocked() {
        return highestUnlocked;
    }

    /** Fewest moves used to clear the level, or 0 if never cleared. */
    public int getBestMoves(int levelNumber) {
        return bestMoves[levelNumber];
    }

    public int getClearedCount() {
        int n = 0;
        for (int i = 1; i < bestMoves.length; i++) {
            if (bestMoves[i] > 0) {
                n++;
            }
        }
        return n;
    }

    /** True for a tile that was missing at level start and a switch built. */
    public boolean isBridge(int row, int col) {
        return initiallyVoid[row][col]
                && !level.getBoard().getTile(row, col).isVoid();
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    // ---------- internals ----------

    private void setUpLevel(Level newLevel) {
        level = newLevel;
        block = new Block(level.getStartRow(), level.getStartCol());
        status = GameRules.Status.PLAYING;
        counter.reset();
        history.clear();
        activatedSwitches.clear();

        Board b = level.getBoard();
        initiallyVoid = new boolean[b.getRows()][b.getCols()];
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                initiallyVoid[r][c] = b.getTile(r, c).isVoid();
            }
        }
        fireChanged();
    }

    private Snapshot snapshot() {
        List<int[]> copy = new ArrayList<>();
        for (int[] sw : activatedSwitches) {
            copy.add(new int[] {sw[0], sw[1]});
        }
        return new Snapshot(block, counter.getMoves(), copy);
    }

    /** A switch fires only when the block lands on it standing upright. */
    private void triggerSwitches() {
        if (!block.isUpright()) {
            return;
        }
        int row = block.getRow();
        int col = block.getCol();
        Tile tile = level.getBoard().getTile(row, col);
        if (tile.isSwitch() && levels.activateSwitch(row, col)) {
            activatedSwitches.add(new int[] {row, col});
        }
    }

    private void recordWin() {
        int n = levels.getCurrentLevelNumber();
        int moves = counter.getMoves();
        if (bestMoves[n] == 0 || moves < bestMoves[n]) {
            bestMoves[n] = moves;
        }
        if (n < levels.getTotalLevels()) {
            highestUnlocked = Math.max(highestUnlocked, n + 1);
        }
    }

    private void fireChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}
