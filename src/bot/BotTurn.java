package bot;

import backend.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        for (var choice : choices) {
            var move = choice.move;
            move.perform();
            choice.evaluation = -search(3);
            move.undo();
        }
        choices.sort(Comparator.comparing(MoveQuality::getEvaluation).reversed());
        System.out.println(choices);
        return choices.get(0).move;
    }

    private int search(int depth) {
        if (depth == 0) {
            return evaluate();
        }
        var state = game.generateMoves();
        if (state.isTerminal()) {
            return state.isCheckmate() ? -Integer.MAX_VALUE : 0;
        }
        var moves = state.moves();
        int bestEvaluation = -Integer.MAX_VALUE;
        for (var move : moves) {
            move.perform();
            int evaluation = -search(depth - 1);
            move.undo();
            bestEvaluation = Math.max(bestEvaluation, evaluation);
        }
        return bestEvaluation;
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
