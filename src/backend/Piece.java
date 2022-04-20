package backend;

import java.util.List;

public final class Piece {
    public enum Type {
        PAWN {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        },
        KNIGHT {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        },
        BISHOP {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        },
        ROOK {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        },
        QUEEN {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        },
        KING {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                return IllegalStateException("Not Implementd");
            }
        };

        abstract List<Move> possibleMoves(Position position, Board board);
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
