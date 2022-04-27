package backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Game {
    private final int THREEFOLD_REPETITION_COUNT = 3;
    static final int FIFTY_MOVE_RULE_PLY_COUNT = 2 * 50;

    private final Board board;

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

    public long getZobristHash() {
        return board.zobrist.getHash();
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
        if (isTooManyMoves()) {
            return new State(State.Type.DRAW_FIFTY_MOVE_RULE, List.of());
        }
        if (isInsufficientMaterial()) {
            return new State(State.Type.DRAW_INSUFFICIENT_MATING, List.of());
        }
        if (isTooManyRepetitions()) {
            return new State(State.Type.DRAW_THREEFOLD_REPETITION, List.of());
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

    /**
     * A draw due to too many moves occurs if both sides have moved 50 times each without pawn moves or piece captures.
     *
     * @return if the game is a draw due to insufficient mating material
     */
    private boolean isTooManyMoves() {
        return board.halfMoveClock >= FIFTY_MOVE_RULE_PLY_COUNT;
    }

    /**
     * A draw due to insufficient mating material occurs when there is a lone king against:
     * 1. a lone king, or
     * 2. a king and a bishop, or
     * 3. a king and a knight, or
     * 4. a king and two knights
     *
     * @return if the game is a draw due to insufficient mating material
     */
    private boolean isInsufficientMaterial() {
        boolean hasBishop = false;
        Color knightColor = null;
        int knightCount = 0;
        for (var line : board.squares) {
            for (var piece : line) {
                if (piece == null || piece.type == Piece.Type.KING) {
                    continue;
                }
                if (piece.type == Piece.Type.BISHOP && !hasBishop && knightCount == 0) {
                    hasBishop = true;
                    continue;
                }
                if (piece.type == Piece.Type.KNIGHT && !hasBishop && knightCount < 2
                        && (knightColor == null || knightColor == piece.color)) {
                    knightCount++;
                    knightColor = piece.color;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * A draw due to too many board repetitions occurs when the board repeats three times, the same player is to move,
     * and the same moves are present (ie: en passant possibility and castling rights).
     *
     * @return if the game is a draw due to too many repetitions
     */
    private boolean isTooManyRepetitions() {
        return board.repetitions.getOrDefault(board.zobrist.getHash(), 0) >= THREEFOLD_REPETITION_COUNT;
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
