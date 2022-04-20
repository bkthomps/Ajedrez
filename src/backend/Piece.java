package backend;

public final class Piece {
    public enum Type {
        PAWN,
        KNIGHT,
        BISHOP,
        ROOK,
        QUEEN,
        KING
    }

    public final Type type;
    public final Color color;

    private Piece() {
        throw new IllegalStateException("Disable default constructor");
    }

    Piece(Type type, Color color) {
        this.type = type;
        this.color = color;
    }
}
