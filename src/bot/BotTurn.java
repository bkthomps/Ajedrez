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
            var transpositions = new HashMap<Long, Integer>();
            for (var choice : choices) {
                choice.evaluation = -evaluateSearch(
                        game, transpositions, choice.move, start,
                        !isBotWhite, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE
                );
                if (System.nanoTime() - start > MAX_NANO_WAIT) {
                    System.out.println("Searched to depth " + (depth - 1));
                    break outer;
                }
            }
            choices.sort(Comparator.comparing(MoveQuality::getEvaluation).reversed());
        }
        return choices.get(0).move;
    }

    private static int evaluateSearch(Game game, Map<Long, Integer> transpositions, Move move, long startTime,
                                      boolean isBotWhite, int depth, int alpha, int beta) {
        long zobristHash = game.getZobristHash();
        if (transpositions.containsKey(zobristHash)) {
            return transpositions.get(zobristHash);
        }
        if (depth <= 0 && !move.hasCapture()) {
            int value = evaluate(game, isBotWhite);
            transpositions.put(zobristHash, value);
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
                    game, transpositions, deeperMove, startTime, !isBotWhite, depth - 1, -beta, -alpha
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

    private static int evaluate(Game game, boolean isWhite) {
        var squares = game.getBoard();
        int totalValue = MaterialWorth.evaluate(squares);
        boolean isLateGame = MaterialWorth.isLateGame;
        totalValue += PieceSquareTables.evaluate(squares, isLateGame);
        totalValue += CastleOpportunity.evaluate(game);
        return isWhite ? totalValue : -totalValue;
    }
}
