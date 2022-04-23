package backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Move {
    final Board board;
    public final Position start;
    public final Position end;

    Move(Board board, Position start, Position end) {
        this.board = board;
        this.start = start;
        this.end = end;
    }

    public abstract Optional<Piece.Type> promotionPieceType();

    public void perform() {
        while (partial()) {
            // No loop body
        }
    }

    public abstract void undo();

    abstract boolean partial();

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
            board.canCastleShort.remove(color);
        } else if (position.column == 0) {
            board.canCastleLong.remove(color);
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
    private List<Color> oldCanCastleShort;
    private List<Color> oldCanCastleLong;
    private Position oldEnPassantTarget;
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
            oldCanCastleShort = new ArrayList<>(board.canCastleShort);
            oldCanCastleLong = new ArrayList<>(board.canCastleLong);
            board.canCastleShort.remove(board.activePlayer);
            board.canCastleLong.remove(board.activePlayer);
            board.activePlayer = board.activePlayer.next();
            oldEnPassantTarget = board.enPassantTarget;
            board.enPassantTarget = null;
            state = State.IN_PROGRESS;
            currentKingColumn = start.column;
            return true;
        }
        if (state == State.IN_PROGRESS) {
            int direction = Integer.signum(end.column - start.column);
            int nextKingColumn = currentKingColumn + direction;
            board.squares[end.row][nextKingColumn] = board.squares[start.row][currentKingColumn];
            board.squares[start.row][currentKingColumn] = null;
            if (nextKingColumn != end.column) {
                currentKingColumn = nextKingColumn;
                return true;
            }
            board.squares[rookEnd.row][rookEnd.column] = board.squares[rookStart.row][rookStart.column];
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
        board.activePlayer = board.activePlayer.previous();
        board.enPassantTarget = oldEnPassantTarget;
        state = State.NOT_STARTED;
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = null;
        board.squares[rookStart.row][rookStart.column] = board.squares[rookEnd.row][rookEnd.column];
        board.squares[rookEnd.row][rookEnd.column] = null;
        board.canCastleShort = oldCanCastleShort;
        board.canCastleLong = oldCanCastleLong;
    }
}

abstract class SingleMove extends Move {
    private Position oldEnPassantTarget;
    private boolean didMove;

    SingleMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    boolean partial() {
        if (didMove) {
            throw new IllegalStateException("Move already performed");
        }
        didMove = true;
        board.activePlayer = board.activePlayer.next();
        oldEnPassantTarget = board.enPassantTarget;
        board.enPassantTarget = null;
        return false;
    }

    @Override
    public void undo() {
        if (!didMove) {
            throw new IllegalStateException("Move not yet performed");
        }
        didMove = false;
        board.activePlayer = board.activePlayer.previous();
        board.enPassantTarget = oldEnPassantTarget;
    }
}

final class PawnPromotion extends SingleMove {
    private Piece captured;
    private Piece original;
    private final Piece promotion;
    private List<Color> oldCanCastleShort;
    private List<Color> oldCanCastleLong;

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
        super.partial();
        oldCanCastleShort = new ArrayList<>(board.canCastleShort);
        oldCanCastleLong = new ArrayList<>(board.canCastleLong);
        updateCastlingRights(end);
        captured = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = promotion;
        original = board.squares[start.row][start.column];
        board.squares[start.row][start.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.squares[start.row][start.column] = original;
        board.squares[end.row][end.column] = captured;
        board.canCastleShort = oldCanCastleShort;
        board.canCastleLong = oldCanCastleLong;
    }
}

final class EnPassant extends SingleMove {
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
        super.partial();
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.squares[start.row][start.column] = null;
        captured = board.squares[pawnCapture.row][pawnCapture.column];
        board.squares[pawnCapture.row][pawnCapture.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = null;
        board.squares[pawnCapture.row][pawnCapture.column] = captured;
    }
}

class RegularMove extends SingleMove {
    private Piece captured;
    private List<Color> oldCanCastleShort;
    private List<Color> oldCanCastleLong;

    RegularMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    public Optional<Piece.Type> promotionPieceType() {
        return Optional.empty();
    }

    @Override
    boolean partial() {
        super.partial();
        oldCanCastleShort = new ArrayList<>(board.canCastleShort);
        oldCanCastleLong = new ArrayList<>(board.canCastleLong);
        updateCastlingRights(end);
        captured = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = board.squares[start.row][start.column];
        board.squares[start.row][start.column] = null;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.squares[start.row][start.column] = board.squares[end.row][end.column];
        board.squares[end.row][end.column] = captured;
        board.canCastleShort = oldCanCastleShort;
        board.canCastleLong = oldCanCastleLong;
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
        super.partial();
        board.enPassantTarget = jumpingOver;
        return false;
    }

    @Override
    public void undo() {
        super.undo();
    }
}

final class ShortRookMove extends RegularMove {
    private List<Color> oldCanCastleShort;

    ShortRookMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    boolean partial() {
        var activePlayer = board.activePlayer;
        super.partial();
        oldCanCastleShort = new ArrayList<>(board.canCastleShort);
        board.canCastleShort.remove(activePlayer);
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.canCastleShort = oldCanCastleShort;
    }
}

final class LongRookMove extends RegularMove {
    private List<Color> oldCanCastleLong;

    LongRookMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    boolean partial() {
        var activePlayer = board.activePlayer;
        super.partial();
        oldCanCastleLong = new ArrayList<>(board.canCastleLong);
        board.canCastleLong.remove(activePlayer);
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.canCastleLong = oldCanCastleLong;
    }
}

final class KingMove extends RegularMove {
    private List<Color> oldCanCastleShort;
    private List<Color> oldCanCastleLong;

    KingMove(Board board, Position start, Position end) {
        super(board, start, end);
    }

    @Override
    boolean partial() {
        oldCanCastleShort = new ArrayList<>(board.canCastleShort);
        oldCanCastleLong = new ArrayList<>(board.canCastleLong);
        board.canCastleShort.remove(board.activePlayer);
        board.canCastleLong.remove(board.activePlayer);
        super.partial();
        return false;
    }

    @Override
    public void undo() {
        super.undo();
        board.canCastleShort = oldCanCastleShort;
        board.canCastleLong = oldCanCastleLong;
    }
}
