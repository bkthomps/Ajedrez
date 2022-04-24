package bot;

import backend.Game;
import backend.State;

public final class BotTurn {
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
