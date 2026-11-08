
package blockescape;

public class HoleTile extends Tile {

    public HoleTile(int row, int col) {
        super(row, col);
    }

    @Override
    public char getSymbol() {
        return 'H';
    }

    @Override
    public boolean isSafe() {
        return false;
    }
}

