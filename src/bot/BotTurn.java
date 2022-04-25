package bot;

import backend.*;

public final class BotTurn {
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
            int moveEvaluation = -evaluateSearch(game, move, !isBotWhite, 3, -Integer.MAX_VALUE, Integer.MAX_VALUE);
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

    private static int evaluateSearch(Game game, Move move, boolean isBotWhite, int depth, int alpha, int beta) {
        if (depth == 0) {
            return evaluate(game.getBoard(), isBotWhite);
        }
        move.perform();
        var state = game.generateMoves();
        if (state.isTerminal()) {
            move.undo();
            return state.isCheckmate() ? -Integer.MAX_VALUE : 0;
        }
        for (var deeperMove : state.moves()) {
            int evaluation = -evaluateSearch(game, deeperMove, !isBotWhite, depth - 1, -beta, -alpha);
            if (evaluation >= beta) {
                move.undo();
                return beta;
            }
            alpha = Math.max(alpha, evaluation);
        }
        move.undo();
        return alpha;
    }

    private static int evaluate(Piece[][] squares, boolean isWhite) {
        var queenCount = new int[]{0, 0};
        var rookCount = new int[]{0, 0};
        var minorCount = new int[]{0, 0};
        var kings = new Position[]{null, null};
        int totalValue = 0;
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[i].length; j++) {
                var piece = squares[i][j];
                if (piece == null) {
                    continue;
                }
                int value = 0;
                switch (piece.type) {
                    case PAWN -> value += PAWN_VALUE;
                    case KNIGHT -> {
                        value += KNIGHT_VALUE;
                        minorCount[piece.color.bitIndex()]++;
                    }
                    case BISHOP -> {
                        value += BISHOP_VALUE;
                        minorCount[piece.color.bitIndex()]++;
                    }
                    case ROOK -> {
                        value += ROOK_VALUE;
                        rookCount[piece.color.bitIndex()]++;
                    }
                    case QUEEN -> {
                        value += QUEEN_VALUE;
                        queenCount[piece.color.bitIndex()]++;
                    }
                    case KING -> kings[piece.color.bitIndex()] = new Position(i, j);
                }
                if (piece.type != Piece.Type.KING) {
                    value += PieceSquareTables.evaluate(squares, i, j, false);
                }
                int factor = (piece.color == Color.WHITE) ? 1 : -1;
                totalValue += factor * value;
            }
        }
        if (kings[0] == null || kings[1] == null) {
            throw new IllegalStateException("No king found");
        }
        boolean isLateGame = isLateGame(queenCount[0], rookCount[0], minorCount[0])
                && isLateGame(queenCount[1], rookCount[1], minorCount[1]);
        totalValue += PieceSquareTables.evaluate(squares, kings[0].row, kings[0].column, isLateGame);
        totalValue -= PieceSquareTables.evaluate(squares, kings[1].row, kings[1].column, isLateGame);
        return isWhite ? totalValue : -totalValue;
    }

    private static boolean isLateGame(int queenCount, int rookCount, int minorCount) {
        return queenCount == 0 || (queenCount == 1 && rookCount == 0 && minorCount <= 1);
    }
}
