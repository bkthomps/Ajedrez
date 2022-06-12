package bot;

import backend.Color;
import backend.Piece;

final class MaterialWorth {
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    private static final int LATE_GAME_CUTOFF = 1500;

    static Boolean isLateGame;

    static int evaluate(Piece[][] squares) {
        int whiteValue = 0;
        int blackValue = 0;
        for (var slice : squares) {
            for (var piece : slice) {
                if (piece == null) {
                    continue;
                }
                int value = 0;
                switch (piece.type) {
                    case PAWN -> value += PAWN_VALUE;
                    case KNIGHT -> value += KNIGHT_VALUE;
                    case BISHOP -> value += BISHOP_VALUE;
                    case ROOK -> value += ROOK_VALUE;
                    case QUEEN -> value += QUEEN_VALUE;
                }
                if (piece.color == Color.WHITE) {
                    whiteValue += value;
                } else {
                    blackValue += value;
                }
            }
        }
        isLateGame = whiteValue < LATE_GAME_CUTOFF && blackValue < LATE_GAME_CUTOFF;
        return whiteValue - blackValue;
    }
}
