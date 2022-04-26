package backend;

import java.util.BitSet;
import java.util.Optional;

public abstract class Move {
    public final Position start;
    public final Position end;

    final Board board;

    private Position oldEnPassantTarget;
    private BitSet oldCanCastleShort;
    private BitSet oldCanCastleLong;
    private boolean called;
    private int oldPlyCount;

    Move(Board board, Position start, Position end) {
        this.board = board;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        var piece = board.squares[start.row][start.column];
        return piece.color + " " + piece.type + " " + start + " -> " + end;
    }

    public abstract Optional<Piece.Type> promotionPieceType();

    public void perform() {
        while (partial()) {
            // No loop body
        }
    }

    boolean partial() {
        if (!called) {
            called = true;
        }
        oldPlyCount = board.plyCount.get(board.activePlayer);
        board.plyCount.put(board.activePlayer, oldPlyCount + 1);
        board.activePlayer = board.activePlayer.next();
        board.zobrist.togglePlayer();
        oldCanCastleShort = (BitSet) board.shortCastleRights.clone();
        oldCanCastleLong = (BitSet) board.longCastleRights.clone();
        oldEnPassantTarget = board.enPassantTarget;
        board.enPassantTarget = null;
        board.zobrist.toggleEnPassant(oldEnPassantTarget);
        return false;
    }

    public void undo() {
        if (!called) {
            throw new IllegalStateException("Cannot undo a move that has not been made");
        }
        board.activePlayer = board.activePlayer.previous();
        board.plyCount.put(board.activePlayer, oldPlyCount);
        board.zobrist.resetShort(board.shortCastleRights, oldCanCastleShort);
        board.zobrist.resetLong(board.longCastleRights, oldCanCastleLong);
        board.zobrist.togglePlayer();
        board.zobrist.toggleEnPassant(board.enPassantTarget);
        board.zobrist.toggleEnPassant(oldEnPassantTarget);
        board.enPassantTarget = oldEnPassantTarget;
        board.shortCastleRights = oldCanCastleShort;
        board.longCastleRights = oldCanCastleLong;
    }

    void updateCastlingRights(Position position) {
        var piece = board.get(position);
        if (piece.isEmpty()) {
            return;
        }
        if (piece.get().type != Piece.Type.ROOK) {
            return;
        }
        var color = piece.get().color;
        if (position.row != color.piecesRow()) {
            return;
        }
        if (position.column == Board.COLUMN_COUNT - 1) {
            board.zobrist.clearShort(color, board.shortCastleRights);
            board.shortCastleRights.clear(color.bitIndex());
        } else if (position.column == 0) {
            board.zobrist.clearLong(color, board.longCastleRights);
            board.longCastleRights.clear(color.bitIndex());
        }
    }
}

final class Castling extends Move {
    private enum State {
        NOT_STARTED,
        IN_PROGRESS,
        DONE
    }

    private final Position rookStart;
    private final Position rookEnd;
    private State state = State.NOT_STARTED;
    private int currentKingColumn;

    Castling(Board board, Position kingStart, Position kingEnd, Position rookStart, Position rookEnd) {
        super(board, kingStart, kingEnd);
        this.rookStart = rookStart;
        this.rookEnd = rookEnd;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    boolean partial() {
        if (state == State.NOT_STARTED) {
            var player = board.activePlayer;
            super.partial();
            board.zobrist.clearShort(player, board.shortCastleRights);
            board.zobrist.clearLong(player, board.longCastleRights);
            board.shortCastleRights.clear(player.bitIndex());
            board.longCastleRights.clear(player.bitIndex());
            state = State.IN_PROGRESS;
            currentKingColumn = start.column;
            return true;
        }
        if (state == State.IN_PROGRESS) {
            int direction = Integer.signum(end.column - start.column);
            int nextKingColumn = currentKingColumn + direction;
            board.zobrist.togglePiece(board.squares, start.row, currentKingColumn);
            board.squares[end.row][nextKingColumn] = board.squares[start.row][currentKingColumn];
            board.zobrist.togglePiece(board.squares, end.row, nextKingColumn);
            board.squares[start.row][currentKingColumn] = null;
            if (nextKingColumn != end.column) {
                currentKingColumn = nextKingColumn;
                return true;
            }
            board.zobrist.togglePiece(board.squares, rookStart.row, rookStart.column);
            board.squares[rookEnd.row][rookEnd.column] = board.squares[rookStart.row][rookStart.column];
            board.zobrist.togglePiece(board.squares, rookEnd.row, rookEnd.column);
            board.squares[rookStart.row][rookStart.column] = null;
            state = State.DONE;
            return false;
        }
        throw new IllegalStateException("In an illegal move state");
    }

    @Override
    public void undo() {
        if (state != State.DONE) {
            throw new IllegalStateException("Move not fully performed");
        }
        super.undo();
        state = State.NOT_STARTED;
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[end.row][end.column] = null;
        board.zobrist.togglePiece(board.squares, rookEnd.row, rookEnd.column);
        board.squares[rookStart.row][rookStart.column] = board.squares[rookEnd.row][rookEnd.column];
        board.zobrist.togglePiece(board.squares, rookStart.row, rookStart.column);
        board.squares[rookEnd.row][rookEnd.column] = null;
    }
}

final class PawnPromotion extends Move {
    private Piece captured;
    private Piece original;
    private final Piece promotion;

    PawnPromotion(Board board, Position start, Position end, Piece promotion) {
        super(board, start, end);
        this.promotion = promotion;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.of(promotion.type);
    }

    @Override
    boolean partial() {
        var player = board.activePlayer;
        super.partial();
        board.plyCount.put(player, 0);
        updateCastlingRights(end);
        captured = board.squares[end.row][end.column];
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[end.row][end.column] = promotion;
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        original = board.squares[start.row][start.column];
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[start.row][start.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.squares[start.row][start.column] = original;
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[end.row][end.column] = captured;
        board.zobrist.togglePiece(board.squares, end.row, end.column);
    }
}

final class EnPassant extends Move {
    private Piece captured;
    private final Position pawnCapture;

    EnPassant(Board board, Position start, Position end, Position pawnCapture) {
        super(board, start, end);
        this.pawnCapture = pawnCapture;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    boolean partial() {
        var player = board.activePlayer;
        super.partial();
        board.plyCount.put(player, 0);
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[start.row][start.column] = null;
        captured = board.squares[pawnCapture.row][pawnCapture.column];
        board.zobrist.togglePiece(board.squares, pawnCapture.row, pawnCapture.column);
        board.squares[pawnCapture.row][pawnCapture.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[end.row][end.column] = null;
        board.squares[pawnCapture.row][pawnCapture.column] = captured;
        board.zobrist.togglePiece(board.squares, pawnCapture.row, pawnCapture.column);
    }
}

class RegularMove extends Move {
    private Piece captured;

    RegularMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    boolean partial() {
        var player = board.activePlayer;
        super.partial();
        updateCastlingRights(start);
        updateCastlingRights(end);
        captured = board.squares[end.row][end.column];
        if (captured != null) {
            board.plyCount.put(player, 0);
        }
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[start.row][start.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.zobrist.togglePiece(board.squares, end.row, end.column);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.zobrist.togglePiece(board.squares, start.row, start.column);
        board.squares[end.row][end.column] = captured;
        board.zobrist.togglePiece(board.squares, end.row, end.column);
    }
}

final class PawnJump extends RegularMove {
    private final Position jumpingOver;

    PawnJump(Board board, Position start, Position end, Position jumpingOver) {
        super(board, start, end);
        this.jumpingOver = jumpingOver;
    }

    @Override
    boolean partial() {
        var player = board.activePlayer;
        super.partial();
        board.plyCount.put(player, 0);
        board.enPassantTarget = jumpingOver;
        board.zobrist.toggleEnPassant(board.enPassantTarget);
        return false;
    }

    @Override
    public void undo() {
        super.undo();
    }
}

final class KingMove extends RegularMove {
    KingMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    boolean partial() {
        var player = board.activePlayer;
        super.partial();
        board.zobrist.clearShort(player, board.shortCastleRights);
        board.zobrist.clearLong(player, board.longCastleRights);
        board.shortCastleRights.clear(player.bitIndex());
        board.longCastleRights.clear(player.bitIndex());
        return false;
    }

    @Override
    public void undo() {
        super.undo();
    }
}
