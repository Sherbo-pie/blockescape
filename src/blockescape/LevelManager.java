package blockescape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private final List<String[]> layouts;
    private int currentLevelIndex;
    private Board currentBoard;

// Key = switch position, value = tile position
    private final Map<String, int[]> switchTargets = new HashMap<>();

    private String positionKey(int level, int row, int col) {
    return level + ":" + row + ":" + col;
    }

    private void setupSwitchTargets() {
    switchTargets.clear();

    // Level 3: switch at (4, 5) creates A at (1, 3)
    switchTargets.put(
        positionKey(3, 4, 5),
        new int[] {1, 3}
    );

    // Level 4: each switch creates one designated A
    switchTargets.put(
        positionKey(4, 0, 4),
        new int[] {2, 8}
    );

    switchTargets.put(
        positionKey(4, 0, 8),
        new int[] {2, 3}
    );

    switchTargets.put(
        positionKey(4, 3, 4),
        new int[] {2, 4}
    );

    switchTargets.put(
        positionKey(4, 3, 8),
        new int[] {5, 4}
    );

    switchTargets.put(
        positionKey(4, 5, 7),
        new int[] {5, 8}
    );
    switchTargets.put(
    positionKey(4, 6, 6),
    new int[] {6, 7}
    );
}

    public LevelManager() {
        setupSwitchTargets();
        layouts = new ArrayList<>();

        // Level 1: Basic movement
        layouts.add(new String[] {
            "XXXXXXX..",
            "...XXXX..",
            ".G......P",
            "...XXXX..",
            "XXXXXXX.."
        });

        // Level 2: Introduces holes
        layouts.add(new String[] {
            "...XXXXX",
            ".G.XXXXX",
            "...XXP..",
            "X.XXX...",
            "X.......",
            "XXXX.H.X",
            "XXXX.H.X",
            "XXXX...X"
        });

        // Level 3: Introduces a switch AND A IS A TILE THAT WILL APPEAR WHEN BUTTON PRESSED
        layouts.add(new String[] {
            "...XX....P",
            ".G.A.....X",
            "...XX...XX",
            "XXXXX..XXX",
            "XXXXXSXXXX"
        });

        layouts.add(new String[] {
            "XXXXSXXXS",
            "...X.XXX.",
            ".G.AAXXXA",
            "...XSXXXS",
            "XXXX.....",
            "XXXXA..SA",
            "XXXXXPSAX"
        });

        
        // Level 5: Challenging holes and orientation changes
        layouts.add(new String[] {
            "XXXXXXXXXXX",
            "X.........X",
            "XX.X....X.X",
            "XP....H...X",
            "X...X..XX.X",
            "XH.....H.GX",
            "X...X.....X",
            "XH...X....X",
            "XXXXXXXXXXX"
        });


        currentLevelIndex = 0;
    }

    /*public void createTile() {
    if (createTileRow >= 0 && createTileCol >= 0) {
        board.setTile(
            createTileRow,
            createTileCol,
            new NormalTile(createTileRow, createTileCol)
        );*/

    public boolean activateSwitch(int switchRow, int switchCol) {
    if (currentBoard == null) {
        return false;
    }

    Tile switchTile = currentBoard.getTile(
        switchRow, switchCol
    );

    if (!(switchTile instanceof SwitchTile)) {
        return false;
    }

    int levelNumber = currentLevelIndex + 1;

    int[] target = switchTargets.get(
        positionKey(levelNumber, switchRow, switchCol)
    );

    if (target == null) {
        return false;
    }

    int targetRow = target[0];
    int targetCol = target[1];

    Tile targetTile = currentBoard.getTile(
        targetRow, targetCol
    );

    // Only create the tile if it is still missing.
    if (!(targetTile instanceof VoidTile)) {
        return false;
    }

    currentBoard.setTile(
        targetRow,
        targetCol,
        new NormalTile(targetRow, targetCol)
    );
/*
    Tile updatedTile = currentBoard.getTile(targetRow, targetCol);

System.out.println("DEBUG: target = ("
    + targetRow + ", " + targetCol + ")");
System.out.println("DEBUG: class = "
    + updatedTile.getClass().getSimpleName());
System.out.println("DEBUG: symbol = "
    + updatedTile.getSymbol());
*/
    return true;
}
    


    public Level loadLevel(int levelNumber) {
        if (levelNumber < 1 ||
            levelNumber > layouts.size()) {
            throw new IllegalArgumentException(
                "Invalid level number: " + levelNumber
            );
        }

        currentLevelIndex = levelNumber - 1;

        String[] layout = layouts.get(currentLevelIndex);

        int rows = layout.length;
        int cols = layout[0].length();
        currentBoard = new Board(rows, cols);
        Board board = currentBoard;


        int startRow = -1;
        int startCol = -1;
        boolean goalFound = false;

        for (int r = 0; r < rows; r++) {
            if (layout[r].length() != cols) {
                throw new IllegalStateException(
                    "All rows must have equal length."
                );
            }

            for (int c = 0; c < cols; c++) {
                char symbol = layout[r].charAt(c);
                Tile tile;

                switch (symbol) {
                    case 'P':
                        startRow = r;
                        startCol = c;
                        tile = new NormalTile(r, c);
                        break;

                    case '.':
                        tile = new NormalTile(r, c);
                        break;

                    case 'H':
                        tile = new HoleTile(r, c);
                        break;

                    case 'G':
                        tile = new GoalTile(r, c);
                        goalFound = true;
                        break;

                    case 'S':
                        tile = new SwitchTile(r, c);
                        break;

                    case 'X':
                        tile = new VoidTile(r, c);
                        break;

                    case 'A':
                        tile = new VoidTile(r, c); // Initially missing
                        break;

                    default:
                        throw new IllegalStateException(
                            "Unknown tile symbol: " + symbol
                        );
                }

                board.setTile(r, c, tile);
            }
        }

        if (startRow == -1 || !goalFound) {
            throw new IllegalStateException(
                "Level needs a start and a goal."
            );
        }

        return new Level(
            levelNumber, board, startRow, startCol
        );
    }

    public Level getCurrentLevel() {
        return loadLevel(currentLevelIndex + 1);
    }

    public Level nextLevel() {
        if (currentLevelIndex + 1 >= layouts.size()) {
            return null;
        }

        return loadLevel(currentLevelIndex + 2);
    }

    public Level restartLevel() {
        return loadLevel(currentLevelIndex + 1);
    }

    public int getTotalLevels() {
        return layouts.size();
    }

    public int getCurrentLevelNumber() {
        return currentLevelIndex + 1;
    }

    public boolean hasNextLevel() {
        return currentLevelIndex + 1 < layouts.size();
    }
}



