package blockescape;

import java.util.Objects;

/**
 * Checks movement outcomes using the existing Board and Tile classes.
 */
public final class GameRules {

    public enum Status {
        PLAYING, FALLEN, WON
    }

    public static final class MoveResult {

        private final Block block;
        private final Status status;

        private MoveResult(Block block, Status status) {
            this.block = block;
            this.status = status;
        }

        public Block getBlock() {
            return block;
        }

        public Status getStatus() {
            return status;
        }
    }

    /**
     * Both occupied cells must be supported when the block is lying down.
     */
    public boolean isSupported(Board board, Block block) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(block, "block");

        for (int[] cell : block.getOccupiedCells()) {
            int row = cell[0];
            int col = cell[1];

            if (!board.isInside(row, col)) {
                return false;
            }

            Tile tile = board.getTile(row, col);

            if (!tile.isSafe() || tile.isVoid()) {
                return false;
            }
        }

        return true;
    }

    public Status evaluate(Board board, Block block) {
        if (!isSupported(board, block)) {
            return Status.FALLEN;
        }

        if (block.isUpright()
                && board.getTile(
                        block.getRow(), block.getCol()).isGoal()) {
            return Status.WON;
        }

        return Status.PLAYING;
    }

    /**
     * Returns the new block and its status.
     * Fallen positions are retained for animation or restart handling.
     * Already won or fallen blocks cannot move.
     */
    public MoveResult move(
            Board board,
            Block block,
            Block.Direction direction) {

        Objects.requireNonNull(direction, "direction");

        Status currentStatus = evaluate(board, block);

        if (currentStatus != Status.PLAYING) {
            return new MoveResult(block, currentStatus);
        }

        Block nextBlock = block.move(direction);
        Status nextStatus = evaluate(board, nextBlock);

        return new MoveResult(nextBlock, nextStatus);
    }
}