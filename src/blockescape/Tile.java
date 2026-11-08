
package blockescape;

public abstract class Tile {

    private final int row;
    private final int col;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // Symbol used to represent this tile
    public abstract char getSymbol();

    // Whether the block can safely stand on this tile
    public boolean isSafe() {
        return true;
    }

    // Whether this tile is the destination
    public boolean isGoal() {
        return false;
    }

    // Whether this tile activates a switch
    public boolean isSwitch() {
        return false;
    }
    
    public boolean isVoid() {
    return false;
    }
}
