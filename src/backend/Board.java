package backend;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class Board {
    static final int ROW_COUNT = 8;
    static final int COLUMN_COUNT = 8;

    final Piece[][] squares = new Piece[ROW_COUNT][COLUMN_COUNT];
    Set<Color> canCastleShort = new HashSet<>();
    Set<Color> canCastleLong = new HashSet<>();
    Position enPassantTarget;
    Color activePlayer;

    Board(String fen) {
        var elements = fen.split(" ");
        setBoard(elements[0]);
        setActivePlayer(elements[1]);
        setCastlingRights(elements[2]);
        setEnPassantTarget(elements[3]);
        // TODO: elements[4] white ply count for 50 move rule
        // TODO: elements[5] black ply count for 50 move rule
    }

    private void setBoard(String positions) {
        var rows = positions.split("/");
        if (rows.length != ROW_COUNT) {
            throw new IllegalArgumentException("The fen string is malformed");
        }
        for (int i = 0; i < rows.length; i++) {
            int j = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    // TODO: support more than single digit numbers
                    j += Character.getNumericValue(c);
                    continue;
                }
                var color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
                var piece = switch (Character.toLowerCase(c)) {
                    case 'p':
                        yield Piece.Type.PAWN;
                    case 'n':
                        yield Piece.Type.KNIGHT;
                    case 'b':
                        yield Piece.Type.BISHOP;
                    case 'r':
                        yield Piece.Type.ROOK;
                    case 'q':
                        yield Piece.Type.QUEEN;
                    case 'k':
                        yield Piece.Type.KING;
                    default:
                        throw new IllegalArgumentException("The fen string is malformed");
                };
                squares[i][j] = new Piece(piece, color);
                j++;
            }
        }
    }

    private void setActivePlayer(String activeColor) {
        if (activeColor.equals("w")) {
            activePlayer = Color.WHITE;
        } else if (activeColor.equals("b")) {
            activePlayer = Color.BLACK;
        } else {
            throw new IllegalArgumentException("The fen string is malformed");
        }
    }

    private void setCastlingRights(String castlingRights) {
        if (castlingRights.equals("-")) {
            return;
        }
        for (char c : castlingRights.toCharArray()) {
            switch (c) {
                case 'K' -> canCastleShort.add(Color.WHITE);
                case 'Q' -> canCastleLong.add(Color.WHITE);
                case 'k' -> canCastleShort.add(Color.BLACK);
                case 'q' -> canCastleLong.add(Color.BLACK);
                default -> throw new IllegalArgumentException("The fen string is malformed");
            }
        }
    }

    private void setEnPassantTarget(String target) {
        if (target.equals("-")) {
            return;
        }
        // TODO: parse algebraic notation of where it can be captured
    }

    private boolean isSquare(Position position) {
        return position.row >= 0 && position.row < ROW_COUNT
                && position.column >= 0 && position.column < COLUMN_COUNT;
    }

    Optional<Piece> get(Position position) {
        if (!isSquare(position)) {
            return Optional.empty();
        }
        var piece = squares[position.row][position.column];
        if (piece == null) {
            return Optional.empty();
        }
        return Optional.of(piece);
    }

    boolean isFree(Position position) {
        return isSquare(position) && squares[position.row][position.column] == null;
    }

    boolean isEnemy(Position position) {
        return get(position).isPresent() && get(position).get().color != activePlayer;
    }
}
