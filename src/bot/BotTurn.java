package bot;

import backend.Game;
import backend.Piece;
import backend.State;

import java.util.HashMap;
import java.util.Map;

public final class BotTurn {
    private static final Map<Piece.Type, Integer> values = new HashMap<>();

    static {
        values.put(Piece.Type.PAWN, 100);
        values.put(Piece.Type.KNIGHT, 300);
        values.put(Piece.Type.BISHOP, 300);
        values.put(Piece.Type.ROOK, 500);
        values.put(Piece.Type.QUEEN, 900);
    }

    public static State perform(Game game) {
        var state = game.generateMoves();
        if (state.isTerminal()) {
            return state;
        }
        var moves = state.moves();
        int size = moves.size();
        int index = (int) (size * Math.random());
        moves.get(index).perform();
        return state;
    }
}
