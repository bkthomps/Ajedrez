package backend;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class Move {
    public final Position start;
    public final Position end;

    private boolean didMove;
    private Position oldEnPassantTarget;

    private Move() {
        throw new IllegalStateException("Disable default constructor");
    }

    Move(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    abstract Optional<Piece.Type> promotionPieceType();

    void doMove(Board board) {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        board.activePlayer = board.activePlayer.next();
        oldEnPassantTarget = board.enPassantTarget;
        board.enPassantTarget = null;
    }

    void undo(Board board) {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board.activePlayer = board.activePlayer.previous();
        board.enPassantTarget = oldEnPassantTarget;
    }
}

final class PawnPromotion extends Move {
    private Piece captured;
    private final Piece promotion;

    PawnPromotion(Position start, Position end, Piece promotion) {
        super(start, end);
        this.promotion = promotion;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.of(promotion.type);
    }

    @Override
    public void doMove(Board board) {
        super.doMove(board);
        captured = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = promotion;
        board.squares[start.row][start.column] = null;
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = captured;
    }
}

final class EnPassant extends Move {
    private Piece captured;
    private final Position pawnCapture;

    EnPassant(Position start, Position end, Position pawnCapture) {
        super(start, end);
        this.pawnCapture = pawnCapture;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    public void doMove(Board board) {
        super.doMove(board);
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.squares[start.row][start.column] = null;
        captured = board.squares[pawnCapture.row][pawnCapture.column];
        board.squares[pawnCapture.row][pawnCapture.column] = null;
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = null;
        board.squares[pawnCapture.row][pawnCapture.column] = captured;
    }
}

class RegularMove extends Move {
    private Piece captured;

    RegularMove(Position start, Position end) {
        super(start, end);
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    public void doMove(Board board) {
        super.doMove(board);
        captured = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.squares[start.row][start.column] = null;
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = captured;
    }
}

class PawnJump extends RegularMove {
    private final Position jumpingOver;

    PawnJump(Position start, Position end, Position jumpingOver) {
        super(start, end);
        this.jumpingOver = jumpingOver;
    }

    @Override
    public void doMove(Board board) {
        super.doMove(board);
        board.enPassantTarget = jumpingOver;
    }
}

class ShortRookMove extends RegularMove {
    private Set<Color> oldCanCastleShort;

    ShortRookMove(Position start, Position end) {
        super(start, end);
    }

    @Override
    public void doMove(Board board) {
        var activePlayer = board.activePlayer;
        super.doMove(board);
        oldCanCastleShort = new HashSet<>(board.canCastleShort);
        board.canCastleShort.remove(activePlayer);
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.canCastleShort = oldCanCastleShort;
    }
}

class LongRookMove extends RegularMove {
    private Set<Color> oldCanCastleLong;

    LongRookMove(Position start, Position end) {
        super(start, end);
    }

    @Override
    public void doMove(Board board) {
        var activePlayer = board.activePlayer;
        super.doMove(board);
        oldCanCastleLong = new HashSet<>(board.canCastleLong);
        board.canCastleLong.remove(activePlayer);
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.canCastleLong = oldCanCastleLong;
    }
}

class KingMove extends RegularMove {
    private Set<Color> oldCanCastleShort;
    private Set<Color> oldCanCastleLong;

    KingMove(Position start, Position end) {
        super(start, end);
    }

    @Override
    public void doMove(Board board) {
        var activePlayer = board.activePlayer;
        super.doMove(board);
        oldCanCastleShort = new HashSet<>(board.canCastleShort);
        oldCanCastleLong = new HashSet<>(board.canCastleLong);
        board.canCastleShort.remove(activePlayer);
        board.canCastleLong.remove(activePlayer);
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.canCastleShort = oldCanCastleShort;
        board.canCastleLong = oldCanCastleLong;
    }
}

final class Castling extends KingMove {
    private final Position rookStart;
    private final Position rookEnd;

    Castling(Position kingStart, Position kingEnd, Position rookStart, Position rookEnd) {
        super(kingStart, kingEnd);
        this.rookStart = rookStart;
        this.rookEnd = rookEnd;
    }

    @Override
    public void doMove(Board board) {
        super.doMove(board);
        board.squares[rookEnd.row][rookEnd.column] = board.squares[rookStart.row][rookStart.column];
        board.squares[rookStart.row][rookStart.column] = null;
    }

    @Override
    public void undo(Board board) {
        super.undo(board);
        board.squares[rookStart.row][rookStart.column] = board.squares[rookEnd.row][rookEnd.column];
        board.squares[rookEnd.row][rookEnd.column] = null;
    }
}
