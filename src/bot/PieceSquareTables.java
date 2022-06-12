package bot;

import backend.Color;
import backend.Piece;

final class PieceSquareTables {
    private static final int[][] PAWN = new int[][]{
            new int[]{ 0,  0,   0,   0,   0,   0,  0,  0},
            new int[]{50, 50,  50,  50,  50,  50, 50, 50},
            new int[]{10, 10,  20,  30,  30,  20, 10, 10},
            new int[]{ 5,  5,  10,  25,  25,  10,  5,  5},
            new int[]{ 0,  0,   0,  20,  20,   0,  0,  0},
            new int[]{ 5, -5, -10,   0,   0, -10, -5,  5},
            new int[]{ 5, 10,  10, -20, -20,  10, 10,  5},
            new int[]{ 0,  0,   0,   0,   0,   0,  0,  0},
    };

    private static final int[][] KNIGHT = new int[][]{
            new int[]{-50, -40, -30, -30, -30, -30, -40, -50},
            new int[]{-40, -20,   0,   0,   0,   0, -20, -40},
            new int[]{-30,   0,  10,  15,  15,  10,   0, -30},
            new int[]{-30,   5,  15,  20,  20,  15,   5, -30},
            new int[]{-30,   0,  15,  20,  20,  15,   0, -30},
            new int[]{-30,   5,  10,  15,  15,  10,   5, -30},
            new int[]{-40, -20,   0,   5,   5,   0, -20, -40},
            new int[]{-50, -40, -30, -30, -30, -30, -40, -50},
    };

    private static final int[][] BISHOP = new int[][]{
            new int[]{-20, -10, -10, -10, -10, -10, -10, -20},
            new int[]{-10,   0,   0,   0,   0,   0,   0, -10},
            new int[]{-10,   0,   5,  10,  10,   5,   0, -10},
            new int[]{-10,   5,   5,  10,  10,   5,   5, -10},
            new int[]{-10,   0,  10,  10,  10,  10,   0, -10},
            new int[]{-10,  10,  10,  10,  10,  10,  10, -10},
            new int[]{-10,   5,   0,   0,   0,   0,   5, -10},
            new int[]{-20, -10, -10, -10, -10, -10, -10, -20},
    };

    private static final int[][] ROOK = new int[][]{
            new int[]{ 0,  0,  0,  0,  0,  0,  0,  0},
            new int[]{ 5, 10, 10, 10, 10, 10, 10,  5},
            new int[]{-5,  0,  0,  0,  0,  0,  0, -5},
            new int[]{-5,  0,  0,  0,  0,  0,  0, -5},
            new int[]{-5,  0,  0,  0,  0,  0,  0, -5},
            new int[]{-5,  0,  0,  0,  0,  0,  0, -5},
            new int[]{-5,  0,  0,  0,  0,  0,  0, -5},
            new int[]{ 0,  0,  0,  5,  5,  0,  0,  0},
    };

    private static final int[][] QUEEN = new int[][]{
            new int[]{-20, -10, -10, -5, -5, -10, -10, -20},
            new int[]{-10,   0,   0,  0,  0,   0,   0, -10},
            new int[]{-10,   0,   5,  5,  5,   5,   0, -10},
            new int[]{ -5,   0,   5,  5,  5,   5,   0,  -5},
            new int[]{  0,   0,   5,  5,  5,   5,   0,  -5},
            new int[]{-10,   5,   5,  5,  5,   5,   0, -10},
            new int[]{-10,   0,   5,  0,  0,   0,   0, -10},
            new int[]{-20, -10, -10, -5, -5, -10, -10, -20},
    };

    private static final int[][] EARLY_GAME_KING = new int[][]{
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-30, -40, -40, -50, -50, -40, -40, -30},
            new int[]{-20, -30, -30, -40, -40, -30, -30, -20},
            new int[]{-10, -20, -20, -20, -20, -20, -20, -10},
            new int[]{ 20,  20,   0,   0,   0,   0,  20,  20},
            new int[]{ 20,  30,  10,   0,   0,  10,  30,  20},
    };

    private static final int[][] LATE_GAME_KING = new int[][]{
            new int[]{-50, -40, -30, -20, -20, -30, -40, -50},
            new int[]{-30, -20, -10,   0,   0, -10, -20, -30},
            new int[]{-30, -10,  20,  30,  30,  20, -10, -30},
            new int[]{-30, -10,  30,  40,  40,  30, -10, -30},
            new int[]{-30, -10,  30,  40,  40,  30, -10, -30},
            new int[]{-30, -10,  20,  30,  30,  20, -10, -30},
            new int[]{-30, -30,   0,   0,   0,   0, -30, -30},
            new int[]{-50, -30, -30, -30, -30, -30, -30, -50},
    };

    static int evaluate(Piece[][] squares, boolean isLateGame) {
        int totalValue = 0;
        for (int i = 0; i < squares.length; i++) {
            for (int column = 0; column < squares[i].length; column++) {
                int row = i;
                var piece = squares[row][column];
                if (piece == null) {
                    continue;
                }
                if (piece.color != Color.WHITE) {
                    row = squares.length - 1 - row;
                }
                int value = 0;
                switch (piece.type) {
                    case PAWN -> value += PAWN[row][column];
                    case KNIGHT -> value += KNIGHT[row][column];
                    case BISHOP -> value += BISHOP[row][column];
                    case ROOK -> value += ROOK[row][column];
                    case QUEEN -> value += QUEEN[row][column];
                    case KING -> {
                        if (isLateGame) {
                            value += LATE_GAME_KING[row][column];
                        } else {
                            value += EARLY_GAME_KING[row][column];
                        }
                    }
                }
                int factor = (piece.color == Color.WHITE) ? 1 : -1;
                totalValue += factor * value;
            }
        }
        return totalValue;
    }
}
