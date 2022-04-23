package backend;

import java.util.ArrayList;
import java.util.List;

public final class Game {
    static final int FIFTY_MOVE_RULE_PLY_COUNT = 50;

    private final Board board;

    private Game() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Game(String fen) {
        board = new Board(fen);
    }

    public Piece[][] getBoard() {
        var squares = new Piece[Board.ROW_COUNT][Board.COLUMN_COUNT];
        for (int i = 0; i < Board.ROW_COUNT; i++) {
            System.arraycopy(board.squares[i], 0, squares[i], 0, Board.COLUMN_COUNT);
        }
        return squares;
    }

    public State generateMoves() {
        var user = board.activePlayer;
        board.activePlayer = user.previous();
        var isKingChecked = isKingChecked(user);
        board.activePlayer = user;
        var alliedPositions = getPiecePositions(user);
        var possibleMoves = possibleMoves(alliedPositions);
        var legalMoves = legalMoves(possibleMoves, user);
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
            var piece = board.get(position).orElseThrow();
            moves.addAll(piece.type.possibleMoves(position, board));
        }
        return moves;
    }

    private Position getKingPosition(List<Position> piecePositions) {
        for (var position : piecePositions) {
            var piece = board.get(position).orElseThrow();
            if (piece.type == Piece.Type.KING) {
                return position;
            }
        }
        throw new IllegalStateException("No allied king");
    }

    private List<Move> legalMoves(List<Move> possibleMoves, Color user) {
        var moves = new ArrayList<Move>();
        for (var move : possibleMoves) {
            boolean hasNextMove = true;
            boolean isLegal = true;
            while (hasNextMove) {
                hasNextMove = move.partial();
                isLegal &= !isKingChecked(user);
            }
            move.undo();
            if (isLegal) {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean isKingChecked(Color user) {
        var allies = getPiecePositions(user);
        var alliedKing = getKingPosition(allies);
        var enemies = getPiecePositions(user.next());
        for (var enemyPosition : enemies) {
            var enemyPiece = board.get(enemyPosition).orElseThrow();
            var moves = enemyPiece.type.possibleMoves(enemyPosition, board);
            for (var move : moves) {
                if (move.end.equals(alliedKing)) {
                    return true;
                }
            }
        }
        return false;
    }
}
