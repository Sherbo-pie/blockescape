
package blockescape;

public class GoalTile extends Tile {

    public GoalTile(int row, int col) {
        super(row, col);
    }

    @Override
    public char getSymbol() {
        return 'G';
    }

    @Override
    public boolean isGoal() {
        return true;
    }
}
