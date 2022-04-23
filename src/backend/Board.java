package backend;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class Board {
    static final int ROW_COUNT = 8;
    static final int COLUMN_COUNT = 8;

    final Piece[][] squares = new Piece[ROW_COUNT][COLUMN_COUNT];
    final Map<Color, Integer> plyCount = new HashMap<>();
    BitSet shortCastleRights = new BitSet();
    BitSet longCastleRights = new BitSet();
    Position enPassantTarget;
    Color activePlayer;

    Board(String fen) {
        var elements = fen.split(" ");
        if (elements.length != 4 && elements.length != 6) {
            throw new IllegalArgumentException("The fen string is malformed: bad attribute count");
        }
        setBoard(elements[0]);
        setActivePlayer(elements[1]);
        setCastlingRights(elements[2]);
        setEnPassantTarget(elements[3]);
        if (elements.length == 6) {
            setPlies(elements[4], elements[5]);
        } else {
            setPlies("0", "0");
        }
        validateKing();
        validateCastling();
    }

    private void setBoard(String positions) {
        var rows = positions.split("/");
        if (rows.length != ROW_COUNT) {
            throw new IllegalArgumentException("The fen string is malformed: row count is incorrect");
        }
        for (int i = 0; i < rows.length; i++) {
            int j = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
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
                        throw new IllegalArgumentException("The fen string is malformed: invalid piece letter");
                };
                if (j > COLUMN_COUNT) {
                    throw new IllegalArgumentException("The fen string is malformed: too many columns");
                }
                squares[i][j] = new Piece(piece, color);
                j++;
            }
            if (j != COLUMN_COUNT) {
                throw new IllegalArgumentException("The fen string is malformed: incorrect column count");
            }
        }
    }

    private void setActivePlayer(String activeColor) {
        if (activeColor.equals("w")) {
            activePlayer = Color.WHITE;
        } else if (activeColor.equals("b")) {
            activePlayer = Color.BLACK;
        } else {
            throw new IllegalArgumentException("The fen string is malformed: invalid active player value");
        }
    }

    private void setCastlingRights(String castlingRights) {
        if (castlingRights.equals("-")) {
            return;
        }
        for (char c : castlingRights.toCharArray()) {
            switch (c) {
                case 'K' -> shortCastleRights.set(Color.WHITE.bitIndex());
                case 'Q' -> longCastleRights.set(Color.WHITE.bitIndex());
                case 'k' -> shortCastleRights.set(Color.BLACK.bitIndex());
                case 'q' -> longCastleRights.set(Color.BLACK.bitIndex());
                default -> throw new IllegalArgumentException("The fen string is malformed: invalid castling value");
            }
        }
    }

    private void setEnPassantTarget(String target) {
        if (target.equals("-")) {
            return;
        }
        var chars = target.toCharArray();
        if (chars.length != 2) {
            throw new IllegalArgumentException("The fen string is malformed: invalid en passant target");
        }
        int column = chars[0] - 'a';
        if (column < 0 || column >= COLUMN_COUNT) {
            throw new IllegalArgumentException("The fen string is malformed: invalid en passant target column");
        }
        int row = Character.getNumericValue(chars[1]);
        if (row < 0 || row >= ROW_COUNT) {
            throw new IllegalArgumentException("The fen string is malformed: invalid en passant target row");
        }
        enPassantTarget = new Position(row, column);
    }

    private void setPlies(String whitePlies, String blackPlies) {
        try {
            int whitePlyCount = Integer.parseInt(whitePlies);
            int blackPlyCount = Integer.parseInt(blackPlies);
            if (whitePlyCount < 0 || blackPlyCount < 0) {
                throw new IllegalArgumentException("The fen string is malformed: ply count less than zero");
            }
            int maxPlyCount = Game.FIFTY_MOVE_RULE_PLY_COUNT;
            if (whitePlyCount > maxPlyCount && blackPlyCount > maxPlyCount) {
                throw new IllegalArgumentException("The fen string is malformed: more plies than 50 move rule allows");
            }
            plyCount.put(Color.WHITE, whitePlyCount);
            plyCount.put(Color.BLACK, blackPlyCount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The fen string is malformed: invalid ply count format");
        }
    }

    private void validateKing() {
        int whiteKingCount = 0;
        int blackKingCount = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (squares[i][j] == null || squares[i][j].type != Piece.Type.KING) {
                    continue;
                }
                if (squares[i][j].color == Color.WHITE) {
                    whiteKingCount++;
                } else {
                    blackKingCount++;
                }
            }
        }
        if (whiteKingCount != 1) {
            throw new IllegalArgumentException("The fen string is malformed: must have one white king");
        }
        if (blackKingCount != 1) {
            throw new IllegalArgumentException("The fen string is malformed: must have one black king");
        }
    }

    private void validateCastling() {
        if (longCastleRights.get(Color.BLACK.bitIndex())) {
            validatePiece(squares[0][0], Piece.Type.ROOK, Color.BLACK);
            validatePiece(squares[0][COLUMN_COUNT / 2], Piece.Type.KING, Color.BLACK);
        }
        if (shortCastleRights.get(Color.BLACK.bitIndex())) {
            validatePiece(squares[0][COLUMN_COUNT - 1], Piece.Type.ROOK, Color.BLACK);
            validatePiece(squares[0][COLUMN_COUNT / 2], Piece.Type.KING, Color.BLACK);
        }
        if (longCastleRights.get(Color.WHITE.bitIndex())) {
            validatePiece(squares[ROW_COUNT - 1][0], Piece.Type.ROOK, Color.WHITE);
            validatePiece(squares[ROW_COUNT - 1][COLUMN_COUNT / 2], Piece.Type.KING, Color.WHITE);
        }
        if (shortCastleRights.get(Color.WHITE.bitIndex())) {
            validatePiece(squares[ROW_COUNT - 1][COLUMN_COUNT - 1], Piece.Type.ROOK, Color.WHITE);
            validatePiece(squares[ROW_COUNT - 1][COLUMN_COUNT / 2], Piece.Type.KING, Color.WHITE);
        }
    }

    private void validatePiece(Piece piece, Piece.Type type, Color color) {
        if (piece == null || piece.type != type || piece.color != color) {
            throw new IllegalArgumentException("The fen string is malformed: invalid castling rights");
        }
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
