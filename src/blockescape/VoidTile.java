
package blockescape;

public class VoidTile extends Tile {

    public VoidTile(int row, int col) {
        super(row, col);
    }

    @Override
    public char getSymbol() {
        return 'X';
    }

    @Override
    public boolean isSafe() {
        return false;
    }
    @Override
    public boolean isVoid() {
        return true;
    }
}
