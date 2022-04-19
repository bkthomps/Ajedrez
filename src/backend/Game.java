package backend;

import java.util.HashSet;
import java.util.Set;

public final class Game {
    private static final int ROW_COUNT = 8;
    private static final int COLUMN_COUNT = 8;

    private final Color userColor;
    // TODO: pass this information into the Move classes
    private final Set<Color> canCastleShort = new HashSet<>();
    private final Set<Color> canCastleLong = new HashSet<>();
    private final Piece[][] board = new Piece[ROW_COUNT][COLUMN_COUNT];
    private Position enPassantTarget;
    private Color activePlayer;

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(Color playerColor) {
        this(playerColor, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Game(Color playerColor, String fen) {
        this.userColor = playerColor;
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
                board[i][j] = new Piece(piece, color);
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
                case 'K':
                    canCastleShort.add(Color.WHITE);
                case 'Q':
                    canCastleLong.add(Color.WHITE);
                case 'k':
                    canCastleShort.add(Color.WHITE);
                case 'q':
                    canCastleLong.add(Color.WHITE);
                default:
                    throw new IllegalArgumentException("The fen string is malformed");
            }
        }
    }

    private void setEnPassantTarget(String enPassantTarget) {
        if (enPassantTarget.equals("-")) {
            return;
        }
        // TODO: parse algebraic notation of where it can be captureed
    }

    public Piece[][] board() {
        return board;
    }

    public State generateMoves() {
        // TODO: generate moves
        throw new IllegalStateException("Not yet implemented");
    }
}
