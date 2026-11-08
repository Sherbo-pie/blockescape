
package blockescape;

public class NormalTile extends Tile {

    public NormalTile(int row, int col) {
        super(row, col);
    }

    @Override
    public char getSymbol() {
        return '.';
    }
}
