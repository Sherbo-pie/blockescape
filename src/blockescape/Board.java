
package blockescape;

public class Board {

    private final int rows;
    private final int cols;
    private final Tile[][] grid;

    public Board(int rows, int cols) {

        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException(
                "Board dimensions must be positive."
            );
        }

        this.rows = rows;
        this.cols = cols;
        this.grid = new Tile[rows][cols];

        // Fill every cell with a normal tile
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new NormalTile(r, c);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    // Check whether a position is inside the board
    public boolean isInside(int row, int col) {
        return row >= 0 && row < rows
            && col >= 0 && col < cols;
    }

    // Return the tile at a specified position
    public Tile getTile(int row, int col) {

        if (!isInside(row, col)) {
            throw new IndexOutOfBoundsException(
                "Position (" + row + ", " + col
                + ") is outside the board."
            );
        }

        return grid[row][col];
    }

    // Replace a tile at a specified position
    public void setTile(int row, int col, Tile tile) {

        if (!isInside(row, col)) {
            throw new IndexOutOfBoundsException(
                "Cannot place tile outside the board."
            );
        }

        if (tile == null) {
            throw new IllegalArgumentException(
                "Tile cannot be null."
            );
        }

        grid[row][col] = tile;
    }

    // Print the board in the console
    public void printBoard() {

        for (int r = 0; r < rows; r++) {

            for (int c = 0; c < cols; c++) {
                System.out.print(
                    grid[r][c].getSymbol() + " "
                );
            }

            System.out.println();
        }
    }
}
