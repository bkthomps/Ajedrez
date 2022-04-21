package backend;

import java.util.ArrayList;
import java.util.List;

public final class Game {
    private final Board board;

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(Color playerColor) {
        this(String.format("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR %s KQkq - 0 1", playerColor.code()));
    }

    public Game(String fen) {
        board = new Board(fen);
    }

    public State generateMoves() {
        var user = board.activePlayer;
        var alliedPositions = getPiecePositions(user);
        var possibleMoves = possibleMoves(alliedPositions);
        var alliedKing = getKingPosition(alliedPositions);
        var legalMoves = legalMoves(possibleMoves, alliedKing, user);
        var isKingChecked = isKingChecked(alliedKing, user);
        if (legalMoves.isEmpty()) {
            var terminalState = isKingChecked ? State.Type.CHECKMATE : State.Type.STALEMATE;
            return new State(terminalState, legalMoves);
        }
        var regularState = isKingChecked ? State.Type.CHECK : State.Type.NORMAL;
        return new State(regularState, legalMoves);
    }

    private List<Position> getPiecePositions(Color player) {
        var positions = new ArrayList<Position>();
        for (int i = 0; i < Board.ROW_COUNT; i++) {
            for (int j = 0; j < Board.COLUMN_COUNT; j++) {
                var piece = board.squares[i][j];
                if (piece != null && piece.color == player) {
                    positions.add(new Position(i, j));
                }
            }
        }
        return positions;
    }

    private List<Move> possibleMoves(List<Position> alliedPositions) {
        var moves = new ArrayList<Move>();
        for (var position : alliedPositions) {
            var piece = board.get(position);
            if (piece.isEmpty()) {
                continue;
            }
            moves.addAll(piece.get().type.possibleMoves(position, board));
        }
        return moves;
    }

    private Position getKingPosition(List<Position> piecePositions) {
        for (var position : piecePositions) {
            var piece = board.get(position);
            if (piece.isPresent() && piece.get().type == Piece.Type.KING) {
                return position;
            }
        }
        throw new IllegalStateException("No allied king");
    }

    private List<Move> legalMoves(List<Move> possibleMoves, Position alliedKing, Color user) {
        var moves = new ArrayList<Move>();
        for (var move : possibleMoves) {
            move.doMove(board);
            var isLegal = !isKingChecked(alliedKing, user);
            move.undo(board);
            if (isLegal) {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean isKingChecked(Position alliedKing, Color user) {
        var enemies = getPiecePositions(user.next());
        for (var enemyPosition : enemies) {
            var enemyPiece = board.get(enemyPosition);
            if (enemyPiece.isEmpty()) {
                continue;
            }
            var moves = enemyPiece.get().type.possibleMoves(enemyPosition, board);
            for (var move : moves) {
                if (move.end.equals(alliedKing)) {
                    return true;
                }
            }
        }
        return false;
    }
}
