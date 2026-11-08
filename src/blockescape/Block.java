package blockescape;

import java.util.Objects;

/**
 * Immutable block position.
 * Rows increase downward; columns increase to the right.
 */
public final class Block {

    public enum Orientation {
        UPRIGHT, HORIZONTAL, VERTICAL
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Anchor: left cell when horizontal, top cell when vertical.
    private final int row;
    private final int col;
    private final Orientation orientation;

    public Block(int row, int col) {
        this(row, col, Orientation.UPRIGHT);
    }

    public Block(int row, int col, Orientation orientation) {
        this.row = row;
        this.col = col;
        this.orientation =
                Objects.requireNonNull(orientation, "orientation");
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public boolean isUpright() {
        return orientation == Orientation.UPRIGHT;
    }

    public int[][] getOccupiedCells() {
        if (orientation == Orientation.HORIZONTAL) {
            return new int[][] {
                {row, col},
                {row, col + 1}
            };
        }

        if (orientation == Orientation.VERTICAL) {
            return new int[][] {
                {row, col},
                {row + 1, col}
            };
        }

        return new int[][] {{row, col}};
    }

    /**
     * Returns a new block after rolling once.
     * GameRules checks whether the landing is safe.
     */
    public Block move(Direction direction) {
        Objects.requireNonNull(direction, "direction");

        switch (orientation) {
            case UPRIGHT:
                switch (direction) {
                    case UP:
                        return new Block(
                                row - 2, col, Orientation.VERTICAL);
                    case DOWN:
                        return new Block(
                                row + 1, col, Orientation.VERTICAL);
                    case LEFT:
                        return new Block(
                                row, col - 2, Orientation.HORIZONTAL);
                    case RIGHT:
                        return new Block(
                                row, col + 1, Orientation.HORIZONTAL);
                }
                break;

            case HORIZONTAL:
                switch (direction) {
                    case UP:
                        return new Block(row - 1, col, orientation);
                    case DOWN:
                        return new Block(row + 1, col, orientation);
                    case LEFT:
                        return new Block(row, col - 1);
                    case RIGHT:
                        return new Block(row, col + 2);
                }
                break;

            case VERTICAL:
                switch (direction) {
                    case UP:
                        return new Block(row - 1, col);
                    case DOWN:
                        return new Block(row + 2, col);
                    case LEFT:
                        return new Block(row, col - 1, orientation);
                    case RIGHT:
                        return new Block(row, col + 1, orientation);
                }
                break;
        }

        throw new AssertionError("Unhandled movement");
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Block)) {
            return false;
        }

        Block block = (Block) other;

        return row == block.row
                && col == block.col
                && orientation == block.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, orientation);
    }

    @Override
    public String toString() {
        return "Block(" + row + ", " + col
                + ", " + orientation + ")";
    }
}