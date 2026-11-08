package blockescape;

public class MoveCounter {
    private int moves;

    public MoveCounter() {
        moves = 0;
    }

    public void recordMove() {
        moves++;
    }

    public int getMoves() {
        return moves;
    }

    public void reset() {
        moves = 0;
    }
}