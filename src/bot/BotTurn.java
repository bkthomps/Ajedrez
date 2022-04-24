package bot;

import backend.*;

public final class BotTurn {
    static final int ROW_COUNT = 8;
    static final int COLUMN_COUNT = 8;
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;

    public static State perform(Game game, boolean isBotWhite) {
        var state = game.generateMoves();
        if (state.isTerminal()) {
            return state;
        }
        Move bestMove = null;
        int bestEvaluation = Integer.MIN_VALUE;
        var moves = state.moves();
        for (var move : moves) {
            int moveEvaluation = -evaluate(game, move, !isBotWhite, 3);
            if (moveEvaluation > bestEvaluation) {
                bestEvaluation = moveEvaluation;
                bestMove = move;
            }
        }
        if (bestMove == null) {
            throw new IllegalStateException("No best move found");
        }
        bestMove.perform();
        return state;
    }

    private static int evaluate(Game game, Move move, boolean isBotWhite, int depth) {
        if (depth == 0) {
            return evaluate(game.getBoard(), isBotWhite);
        }
        move.perform();
        var state = game.generateMoves();
        if (state.isTerminal()) {
            move.undo();
            return state.isCheckmate() ? Integer.MIN_VALUE : 0;
        }
        int bestEvaluation = Integer.MIN_VALUE;
        for (var deeperMove : state.moves()) {
            int evaluation = -evaluate(game, deeperMove, !isBotWhite, depth - 1);
            bestEvaluation = Math.max(evaluation, bestEvaluation);
        }
        move.undo();
        return bestEvaluation;
    }

    private static int evaluate(Piece[][] squares, boolean isWhite) {
        int whiteQueenCount = 0;
        int blackQueenCount = 0;
        int whiteRookCount = 0;
        int blackRookCount = 0;
        int whitePiecesCount = 0;
        int blackPiecesCount = 0;
        Position whiteKing = null;
        Position blackKing = null;
        int value = 0;
        for (int i = 0; i < BotTurn.ROW_COUNT; i++) {
            for (int j = 0; j < BotTurn.COLUMN_COUNT; j++) {
                var piece = squares[i][j];
                if (piece == null) {
                    continue;
                }
                if (piece.color == Color.WHITE) {
                    switch (piece.type) {
                        case PAWN -> value += PAWN_VALUE;
                        case KNIGHT -> {
                            value += KNIGHT_VALUE;
                            whitePiecesCount++;
                        }
                        case BISHOP -> {
                            value += BISHOP_VALUE;
                            whitePiecesCount++;
                        }
                        case ROOK -> {
                            value += ROOK_VALUE;
                            whiteRookCount++;
                        }
                        case QUEEN -> {
                            value += QUEEN_VALUE;
                            whiteQueenCount++;
                        }
                        case KING -> whiteKing = new Position(i, j);
                    }
                    if (piece.type != Piece.Type.KING) {
                        value += PieceSquareTables.evaluate(squares[i][j], i, j, false);
                    }
                } else {
                    switch (piece.type) {
                        case PAWN -> value -= PAWN_VALUE;
                        case KNIGHT -> {
                            value -= KNIGHT_VALUE;
                            blackPiecesCount++;
                        }
                        case BISHOP -> {
                            value -= BISHOP_VALUE;
                            blackPiecesCount++;
                        }
                        case ROOK -> {
                            value -= ROOK_VALUE;
                            blackRookCount++;
                        }
                        case QUEEN -> {
                            value -= QUEEN_VALUE;
                            blackQueenCount++;
                        }
                        case KING -> blackKing = new Position(i, j);
                    }
                    if (piece.type != Piece.Type.KING) {
                        value -= PieceSquareTables.evaluate(squares[i][j], i, j, false);
                    }
                }
            }
        }
        boolean isWhiteEndgame = whiteQueenCount == 0
                || (whiteQueenCount == 1 && whiteRookCount == 0 && whitePiecesCount <= 1);
        boolean isBlackEndgame = blackQueenCount == 0
                || (blackQueenCount == 1 && blackRookCount == 0 && blackPiecesCount <= 1);
        boolean isEndgame = isWhiteEndgame && isBlackEndgame;
        value += PieceSquareTables.evaluate(
                squares[whiteKing.row][whiteKing.column], whiteKing.row, whiteKing.column, isEndgame
        );
        value -= PieceSquareTables.evaluate(
                squares[blackKing.row][blackKing.column], blackKing.row, blackKing.column, isEndgame
        );
        return isWhite ? value : -value;
    }
}
