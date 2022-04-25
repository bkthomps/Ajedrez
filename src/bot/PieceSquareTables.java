package bot;

import backend.Color;
import backend.Piece;
import backend.Position;

final class PieceSquareTables {
    private static final int[][] PAWN = new int[][]{
            new int[]{0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{50, 50, 50, 50, 50, 50, 50, 50},
            new int[]{10, 10, 20, 30, 30, 20, 10, 10},
            new int[]{5, 5, 10, 25, 25, 10, 5, 5},
            new int[]{0, 0, 0, 20, 20, 0, 0, 0},
            new int[]{5, -5, -10, 0, 0, -10, -5, 5},
            new int[]{5, 10, 10, -20, -20, 10, 10, 5},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0},
    };

    private static final int[][] KNIGHT = new int[][]{
            new int[]{-50, -40, -30, -30, -30, -30, -40, -50},
            new int[]{-40, -20, 0, 0, 0, 0, -20, -40},
            new int[]{-30, 0, 10, 15, 15, 10, 0, -30},
            new int[]{-30, 5, 15, 20, 20, 15, 5, -30},
            new int[]{-30, 0, 15, 20, 20, 15, 0, -30},
            new int[]{-30, 5, 10, 15, 15, 10, 5, -30},
            new int[]{-40, -20, 0, 5, 5, 0, -20, -40},
            new int[]{-50, -40, -30, -30, -30, -30, -40, -50},
    };

    private static final int[][] BISHOP = new int[][]{
            new int[]{-20, -10, -10, -10, -10, -10, -10, -20},
            new int[]{-10, 0, 0, 0, 0, 0, 0, -10},
            new int[]{-10, 0, 5, 10, 10, 5, 0, -10},
            new int[]{-10, 5, 5, 10, 10, 5, 5, -10},
            new int[]{-10, 0, 10, 10, 10, 10, 0, -10},
            new int[]{-10, 10, 10, 10, 10, 10, 10, -10},
            new int[]{-10, 5, 0, 0, 0, 0, 5, -10},
            new int[]{-20, -10, -10, -10, -10, -10, -10, -20},
    };

    private static final int[][] ROOK = new int[][]{
            new int[]{0, 0, 0, 0, 0, 0, 0, 0},
            new int[]{5, 10, 10, 10, 10, 10, 10, 5},
            new int[]{-5, 0, 0, 0, 0, 0, 0, -5},
            new int[]{-5, 0, 0, 0, 0, 0, 0, -5},
            new int[]{-5, 0, 0, 0, 0, 0, 0, -5},
            new int[]{-5, 0, 0, 0, 0, 0, 0, -5},
            new int[]{-5, 0, 0, 0, 0, 0, 0, -5},
            new int[]{0, 0, 0, 5, 5, 0, 0, 0},
    };

    private static final int[][] QUEEN = new int[][]{
            new int[]{-20, -10, -10, -5, -5, -10, -10, -20},
            new int[]{-10, 0, 0, 0, 0, 0, 0, -10},
            new int[]{-10, 0, 5, 5, 5, 5, 0, -10},
            new int[]{-5, 0, 5, 5, 5, 5, 0, -5},
            new int[]{0, 0, 5, 5, 5, 5, 0, -5},
            new int[]{-10, 5, 5, 5, 5, 5, 0, -10},
            new int[]{-10, 0, 5, 0, 0, 0, 0, -10},
            new int[]{-20, -10, -10, -5, -5, -10, -10, -20},
    };

    private static final int[][] EARLY_GAME_KING = new int[][]{
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-20, -30, -30, -40, -40, -30, -30, -20},
            new int[]{-10, -20, -20, -20, -20, -20, -20, -10},
            new int[]{20, 20, 0, 0, 0, 0, 20, 20},
            new int[]{20, 30, 10, 0, 0, 10, 30, 20},
    };

    /*
     * Use this table for the king if:
     *   1. Both sides have no queens, or
     *   2. Every side which has a queen has one minor piece maximum
     */
    private static final int[][] LATE_GAME_KING = new int[][]{
            new int[]{-50, -40, -30, -20, -20, -30, -40, -50},
            new int[]{-30, -20, -10, 0, 0, -10, -20, -30},
            new int[]{-30, -10, 20, 30, 30, 20, -10, -30},
            new int[]{-30, -10, 30, 40, 40, 30, -10, -30},
            new int[]{-30, -10, 30, 40, 40, 30, -10, -30},
            new int[]{-30, -10, 20, 30, 30, 20, -10, -30},
            new int[]{-30, -30, 0, 0, 0, 0, -30, -30},
            new int[]{-50, -30, -30, -30, -30, -30, -30, -50},
    };

    static int evaluate(Piece[][] squares, int row, int column, boolean isLateGame) {
        var piece = squares[row][column];
        if (piece.color != Color.WHITE) {
            row = squares.length - 1 - row;
        }
        switch (piece.type) {
            case PAWN:
                return PAWN[row][column];
            case KNIGHT:
                return KNIGHT[row][column];
            case BISHOP:
                return BISHOP[row][column];
            case ROOK:
                return ROOK[row][column];
            case QUEEN:
                return QUEEN[row][column];
            case KING:
                if (isLateGame) {
                    return LATE_GAME_KING[row][column];
                }
                return EARLY_GAME_KING[row][column];
            default:
                throw new IllegalStateException("Invalid piece type for evaluation");
        }
    }
}