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
        // Black pawns advance by increasing the row value
        var alliedPositions = getAlliedPiecePositions();
        var kingPosition = getKingPosition(alliedPositions);
        var possibleMoves = possibleMoves(alliedPositions);
        var legalMoves = legalMoves(possibleMoves, kingPosition);
        var isKingChecked = isKingChecked(kingPosition);
        if (legalMoves.isEmpty()) {
            var terminalState = isKingChecked ? State.Type.CHECKMATE : State.Type.STALEMATE;
            return new State(terminalState, legalMoves);
        }
        var regularState = isKingChecked ? State.Type.CHECK : State.Type.NORMAL;
        return new State(regularState, legalMoves);
    }

    private List<Position> getAlliedPiecePositions() {
        var positions = new ArrayList<Position>();
        for (int i = 0; i < Board.ROW_COUNT; i++) {
            for (int j = 0; j < Board.COLUMN_COUNT; j++) {
                var piece = board.squares[i][j];
                if (piece != null && piece.color == board.activePlayer) {
                    positions.add(new Position(i, j));
                }
            }
        }
        return positions;
    }

    private Position getKingPosition(List<Position> piecePositions) {
        for (var position : piecePositions) {
            var piece = board.squares[position.row][position.column];
            if (piece != null && piece.type == Piece.Type.KING) {
                return position;
            }
        }
        throw new IllegalStateException("No allied king");
    }

    private List<Move> possibleMoves(List<Position> alliedPositions) {
        var moves = new ArrayList<Move>();
        for (var position : alliedPositions) {
            var piece = board.squares[position.row][position.column];
            if (piece == null) {
                continue;
            }
            moves.addAll(piece.type.possibleMoves(position, board));
        }
        return moves;
    }

    private List<Move> legalMoves(List<Move> possibleMoves, Position king) {
        var moves = new ArrayList<Move>();
        for (var move : possibleMoves) {
            move.doMove(board);
            var isLegal = !isKingChecked(king);
            move.undo(board);
            if (isLegal) {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean isKingChecked(Position king) {
        // TODO: implement
        return false;
    }
}
