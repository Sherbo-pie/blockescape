package blockescape;

public class Level {

    private final int levelNumber;
    private final Board board;
    private final int startRow;
    private final int startCol;

    public Level(int levelNumber, Board board,
                 int startRow, int startCol) {
        this.levelNumber = levelNumber;
        this.board = board;
        this.startRow = startRow;
        this.startCol = startCol;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public Board getBoard() {
        return board;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }
}