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
        var alliedKingPosition = getKingPosition(alliedPositions);
        // TODO: check if checkmate or stalemate or draw
        // TODO: for each piece, check where it can move
        // TODO: for each possible move, find out legal moves (won't put king in check)
        throw new IllegalStateException("TODO");
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
}
