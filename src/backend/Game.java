package backend;

import java.util.HashSet;
import java.util.Set;

public final class Game {
    private final Color userColor;
    private final Set<Color> cannotCastle = new HashSet<>();
    private final Piece[][] board = new Piece[8][8];

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(Color playerColor) {
        this.userColor = playerColor;
        // TODO: setup board
        throw new IllegalStateException("Not yet implemented");
    }

    public Piece[][] board() {
        return board;
    }

    public State generateMoves() {
        // TODO: generate moves
        throw new IllegalStateException("Not yet implemented");
    }
}
