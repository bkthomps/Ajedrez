package backend;

public final class Game {
    private final Color userColor;
    private final Board board;

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(Color playerColor) {
        this(String.format("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR %s KQkq - 0 1", playerColor.code()));
    }

    public Game(String fen) {
        board = new Board(fen);
        userColor = board.activePlayer;
    }

    public State generateMoves() {
        // TODO: generate moves
        throw new IllegalStateException("Not yet implemented");
    }
}
