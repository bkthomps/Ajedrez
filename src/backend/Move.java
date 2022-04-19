package backend;

import java.util.Optional;

public interface Move {
    Position start();

    Position end();

    Optional<Piece.Type> promotionPieceType();

    void doMove(Piece[][] board);

    void undo(Piece[][] board);
}

final class Regular implements Move {
    private final Position start;
    private final Position end;
    private boolean didMove;
    private Piece captured;

    Regular(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Position start() {
        return start;
    }

    @Override
    public Position end() {
        return end;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    public void doMove(Piece[][] board) {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        captured = board[end.row][end.column];
        board[end.row][end.column] = board[start.row][start.column];
        board[start.row][start.column] = null;
    }

    @Override
    public void undo(Piece[][] board) {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board[start.row][start.column] = board[end.row][end.column];
        board[end.row][end.column] = captured;
    }
}

final class EnPassant implements Move {
    private final Position start;
    private final Position end;
    private final Position pawnCapture;
    private boolean didMove;
    private Piece capturedPawn;

    EnPassant(Position start, Position end, Position pawnCapture) {
        this.start = start;
        this.end = end;
        this.pawnCapture = pawnCapture;
    }

    @Override
    public Position start() {
        return start;
    }

    @Override
    public Position end() {
        return end;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    public void doMove(Piece[][] board) {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        board[end.row][end.column] = board[start.row][start.column];
        board[start.row][start.column] = null;
        capturedPawn = board[pawnCapture.row][pawnCapture.column];
        board[pawnCapture.row][pawnCapture.column] = null;
    }

    @Override
    public void undo(Piece[][] board) {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board[start.row][start.column] = board[end.row][end.column];
        board[end.row][end.column] = null;
        board[pawnCapture.row][pawnCapture.column] = capturedPawn;
    }
}

final class Castling implements Move {
    private Position kingStart;
    private Position kingEnd;
    private Position rookStart;
    private Position rookEnd;
    private boolean didMove;

    void kingMovement(Position kingStart, Position kingEnd) {
        this.kingStart = kingStart;
        this.kingEnd = kingEnd;
    }

    void rookMovement(Position rookStart, Position rookEnd) {
        this.rookStart = rookStart;
        this.rookEnd = rookEnd;
    }

    @Override
    public Position start() {
        return kingStart;
    }

    @Override
    public Position end() {
        return kingEnd;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    public void doMove(Piece[][] board) {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        board[kingEnd.row][kingEnd.column] = board[kingStart.row][kingStart.column];
        board[kingStart.row][kingStart.column] = null;
        board[rookEnd.row][rookEnd.column] = board[rookStart.row][rookStart.column];
        board[rookStart.row][rookStart.column] = null;
    }

    @Override
    public void undo(Piece[][] board) {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board[kingStart.row][kingStart.column] = board[kingEnd.row][kingEnd.column];
        board[kingEnd.row][kingEnd.column] = null;
        board[rookStart.row][rookStart.column] = board[rookEnd.row][rookEnd.column];
        board[rookEnd.row][rookEnd.column] = null;
    }
}

final class PawnPromotion implements Move {
    private final Position start;
    private final Position end;
    private final Piece promotion;
    private boolean didMove;
    private Piece captured;

    PawnPromotion(Position start, Position end, Piece promotion) {
        this.start = start;
        this.end = end;
        this.promotion = promotion;
    }

    @Override
    public Position start() {
        return start;
    }

    @Override
    public Position end() {
        return end;
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.of(promotion.type);
    }

    @Override
    public void doMove(Piece[][] board) {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        captured = board[end.row][end.column];
        board[end.row][end.column] = promotion;
        board[start.row][start.column] = null;
    }

    @Override
    public void undo(Piece[][] board) {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board[start.row][start.column] = board[end.row][end.column];
        board[end.row][end.column] = captured;
    }
}
