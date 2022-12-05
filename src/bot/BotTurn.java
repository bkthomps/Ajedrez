package bot;

import backend.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BotTurn {
    private static final int MAX_WAIT_SECONDS = 3;
    private static final long NANO_SECONDS_PER_SECOND = 1_000_000_000;
    private static final long MAX_NANO_WAIT = MAX_WAIT_SECONDS * NANO_SECONDS_PER_SECOND;
    private final Game game;

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

    public static State perform(Game game) {
        var state = game.generateMoves();
        if (state.isTerminal()) {
            return state;
        }
        var moves = state.moves();
        var bot = new BotTurn(game);
        var bestMove = bot.getBestMove(moves);
        bestMove.perform();
        return state;
    }

    private BotTurn(Game game) {
        this.game = game;
    }

    private Move getBestMove(List<Move> moves) {
        var choices = new ArrayList<MoveQuality>(moves.size());
        for (var move : moves) {
            choices.add(new MoveQuality(move));
        }
        return getBestMoveChoice(choices);
    }

    private Move getBestMoveChoice(List<MoveQuality> choices) {
        System.out.println();
        var startTime = System.nanoTime();
        for (int depth = 0; ; depth++) {
            var transpositions = new HashMap<Long, Integer>();
            boolean furtherDepth = getBestChoicesInPlace(choices, depth, startTime, transpositions);
            if (!furtherDepth) {
                break;
            }
        }
        return choices.get(0).move;
    }

    private boolean getBestChoicesInPlace(List<MoveQuality> choices, int depth, long startTime,
                                          Map<Long, Integer> transpositions) {
        for (var choice : choices) {
            var move = choice.move;
            move.perform();
            var evaluation = search(depth, startTime, -Integer.MAX_VALUE, Integer.MAX_VALUE, transpositions);
            evaluation.ifPresent(eval -> choice.evaluation = -eval);
            move.undo();
            if (evaluation.isEmpty()) {
                return false;
            }
        }
        choices.sort(Comparator.comparing(MoveQuality::getEvaluation).reversed());
        System.out.println("Searched to depth " + depth + ": " + choices);
        return true;
    }

    private Optional<Integer> search(int depth, long startTime, int alpha, int beta,
                                     Map<Long, Integer> transpositions) {
        var zobristHash = game.getZobristHash() + depth;
        if (transpositions.containsKey(zobristHash)) {
            return Optional.of(transpositions.get(zobristHash));
        }
        if (depth == 0) {
            var value = evaluate();
            transpositions.put(zobristHash, value);
            return Optional.of(value);
        }
        if (System.nanoTime() - startTime > MAX_NANO_WAIT) {
            return Optional.empty();
        }
        var state = game.generateMoves();
        if (state.isTerminal()) {
            return Optional.of(state.isCheckmate() ? -Integer.MAX_VALUE : 0);
        }
        var moves = state.moves();
        for (var move : moves) {
            move.perform();
            var evaluation = search(depth - 1, startTime, -beta, -alpha, transpositions);
            move.undo();
            if (evaluation.isPresent()) {
                var eval = -evaluation.get();
                if (eval >= beta) {
                    return Optional.of(beta);
                }
                alpha = Math.max(alpha, eval);
            }
        }
        return Optional.of(alpha);
    }

    private int evaluate() {
        var squares = game.getBoard();
        int totalValue = MaterialWorth.evaluate(squares);
        boolean isLateGame = MaterialWorth.isLateGame;
        totalValue += PieceSquareTables.evaluate(squares, isLateGame);
        totalValue += CastleOpportunity.evaluate(game);
        return (game.getActivePlayer() == Color.WHITE) ? totalValue : -totalValue;
    }
}
