package backend;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class Zobrist {
    private static final Random generator = new Random();
    private static final long blackToMove = generator.nextLong();
    private static final Map<Piece, long[][]> pieces = new HashMap<>();
    private static final Map<Color, Long> shortCastleRights = new HashMap<>();
    private static final Map<Color, Long> longCastleRights = new HashMap<>();
    private static final long[] enPassantColumn = new long[Board.COLUMN_COUNT];

    static {
        for (var color : Color.values()) {
            for (var type : Piece.Type.values()) {
                long[][] squares = new long[Board.ROW_COUNT][Board.COLUMN_COUNT];
                for (int i = 0; i < squares.length; i++) {
                    for (int j = 0; j < squares[i].length; j++) {
                        squares[i][j] = generator.nextLong();
                    }
                }
                pieces.put(new Piece(type, color), squares);
            }
            shortCastleRights.put(color, generator.nextLong());
            longCastleRights.put(color, generator.nextLong());
        }
        for (int i = 0; i < enPassantColumn.length; i++) {
            enPassantColumn[i] = generator.nextLong();
        }
    }

    private long hash;

    Zobrist(Color activePlayer, Piece[][] squares, BitSet shortCastle, BitSet longCastle, Position enPassant) {
        hash = (activePlayer == Color.WHITE) ? 0 : blackToMove;
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[i].length; j++) {
                var piece = squares[i][j];
                if (piece == null) {
                    continue;
                }
                hash ^= pieces.get(piece)[i][j];
            }
        }
        for (var color : Color.values()) {
            if (shortCastle.get(color.bitIndex())) {
                hash ^= shortCastleRights.get(color);
            }
            if (longCastle.get(color.bitIndex())) {
                hash ^= longCastleRights.get(color);
            }
        }
        if (enPassant != null) {
            hash ^= enPassantColumn[enPassant.column];
        }
    }

    long getHash() {
        return hash;
    }

    void togglePlayer() {
        hash ^= blackToMove;
    }

    void togglePiece(Piece[][] squares, int row, int column) {
        var piece = squares[row][column];
        if (piece == null) {
            return;
        }
        hash ^= pieces.get(piece)[row][column];
    }

    void toggleEnPassant(Position position) {
        if (position == null) {
            return;
        }
        hash ^= enPassantColumn[position.row];
    }

    void clearShort(Color player, BitSet oldRights) {
        if (oldRights.get(player.bitIndex())) {
            hash ^= shortCastleRights.get(player);
        }
    }

    void clearLong(Color player, BitSet oldRights) {
        if (oldRights.get(player.bitIndex())) {
            hash ^= longCastleRights.get(player);
        }
    }

    void resetShort(BitSet modified, BitSet copied) {
        for (var color : Color.values()) {
            if (modified.get(color.bitIndex()) != copied.get(color.bitIndex())) {
                hash ^= shortCastleRights.get(color);
            }
        }
    }

    void resetLong(BitSet modified, BitSet copied) {
        for (var color : Color.values()) {
            if (modified.get(color.bitIndex()) != copied.get(color.bitIndex())) {
                hash ^= longCastleRights.get(color);
            }
        }
    }
}
