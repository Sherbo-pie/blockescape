package blockescape;

public class SwitchTile extends Tile {

    public SwitchTile(int row, int col) {
        super(row, col);
    }

    @Override
    public char getSymbol() {
        return 'S';
    }

    @Override
    public boolean isSwitch() {
        return true;
    }
}
