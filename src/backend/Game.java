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
        var alliedPositions = getPiecePositions(board.activePlayer);
        var possibleMoves = possibleMoves(alliedPositions);
        var legalMoves = legalMoves(possibleMoves);
        var isKingChecked = isKingChecked();
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
            var piece = board.squares[position.row][position.column];
            if (piece == null) {
                continue;
            }
            moves.addAll(piece.type.possibleMoves(position, board));
        }
        return moves;
    }

    private List<Move> legalMoves(List<Move> possibleMoves) {
        var moves = new ArrayList<Move>();
        for (var move : possibleMoves) {
            move.doMove(board);
            var isLegal = !isKingChecked();
            move.undo(board);
            if (isLegal) {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean isKingChecked() {
        var enemies = getPiecePositions(board.activePlayer.next());
        for (var enemyPosition : enemies) {
            var enemyPiece = board.get(enemyPosition);
            if (enemyPiece.isPresent() && enemyPiece.get().color == board.activePlayer) {
                return true;
            }
        }
        return false;
    }
}
