package bot;

import backend.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BotTurn {
    private static final int MAX_WAIT_SECONDS = 3;
    private static final long NANO_SECONDS_PER_SECOND = 1_000_000_000;
    private static final long MAX_NANO_WAIT = MAX_WAIT_SECONDS * NANO_SECONDS_PER_SECOND;

    private static final class MoveQuality {
        private final Move move;
        private int evaluation;

        private MoveQuality(Move move) {
            this.move = move;
            this.evaluation = Integer.MIN_VALUE;
        }

        private int getEvaluation() {
            return evaluation;
        }

        @Override
        public String toString() {
            return move + " = " + evaluation;
        }
    }

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
        var moves = state.moves();
        var bestMove = getBestMove(game, moves, isBotWhite);
        bestMove.perform();
        return state;
    }

    private static Move getBestMove(Game game, List<Move> rawMoves, boolean isBotWhite) {
        var choices = new ArrayList<MoveQuality>();
        for (var move : rawMoves) {
            choices.add(new MoveQuality(move));
        }
        long start = System.nanoTime();
        outer:
        for (int depth = 0; ; depth++) {
            var tabulation = new HashMap<Long, Integer>();
            for (int i = 0; i < choices.size(); i++) {
                var choice = choices.get(i);
                choice.evaluation = -evaluateSearch(
                        game, tabulation, choice.move, start, !isBotWhite, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE
                );
                if (System.nanoTime() - start > MAX_NANO_WAIT) {
                    System.out.println("Aborting depth " + depth + " after " + i * 100 / choices.size() + " percent");
                    break outer;
                }
            }
            choices.sort(Comparator.comparing(MoveQuality::getEvaluation).reversed());
            System.out.println("Finished depth of " + depth);
        }
        return choices.get(0).move;
    }

    private static int evaluateSearch(Game game, Map<Long, Integer> tabulation, Move move, long startTime,
                                      boolean isBotWhite, int depth, int alpha, int beta) {
        long zobristHash = game.getZobristHash();
        if (tabulation.containsKey(zobristHash)) {
            return tabulation.get(zobristHash);
        }
        if (depth == 0) {
            int value = evaluate(game.getBoard(), isBotWhite);
            tabulation.put(zobristHash, value);
            return value;
        }
        if (System.nanoTime() - startTime > MAX_NANO_WAIT) {
            return 0;
        }
        move.perform();
        var state = game.generateMoves();
        if (state.isTerminal()) {
            move.undo();
            return state.isCheckmate() ? -Integer.MAX_VALUE : 0;
        }
        for (var deeperMove : state.moves()) {
            int evaluation = -evaluateSearch(
                    game, tabulation, deeperMove, startTime, !isBotWhite, depth - 1, -beta, -alpha
            );
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
