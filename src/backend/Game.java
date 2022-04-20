package backend;

public final class Game {
    private final Color userColor;
    private final Board board;

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(Color playerColor) {
        this(playerColor, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Game(Color playerColor, String fen) {
        userColor = playerColor;
        board = new Board(fen);
    }

    public State generateMoves() {
        // TODO: generate moves
        throw new IllegalStateException("Not yet implemented");
    }
}
