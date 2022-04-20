package backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Piece {
    public enum Type {
        PAWN {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                var piece = board.get(position).orElseThrow();
                if (position.row + piece.color.pawnMove() == piece.color.endRow()) {
                    return pawnPromotions(position, board, piece);
                }
                var moves = new ArrayList<Move>();
                pawnJump(position, board, piece).ifPresent(moves::add);
                moves.addAll(normalMoves(position, board, piece));
                enPassant(position, board, piece).ifPresent(moves::add);
                return moves;
            }

            private List<Move> pawnPromotions(Position position, Board board, Piece piece) {
                var moves = new ArrayList<Move>();
                var advance = new Position(piece.color.endRow(), position.column);
                var captureLeft = new Position(piece.color.endRow(), position.column - 1);
                var captureRight = new Position(piece.color.endRow(), position.column + 1);
                if (board.get(advance).isEmpty()) {
                    moves.addAll(pawnPromotions(position, advance, board));
                }
                var captures = new Position[]{captureLeft, captureRight};
                for (var capture : captures) {
                    if (board.get(capture).isEmpty()) {
                        continue;
                    }
                    var capturePiece = board.get(capture).get();
                    if (capturePiece.color != board.activePlayer) {
                        moves.addAll(pawnPromotions(position, capture, board));
                    }
                }
                return moves;
            }

            private List<Move> pawnPromotions(Position start, Position end, Board board) {
                var moves = new ArrayList<Move>();
                var promotablePieces = new Type[]{KNIGHT, BISHOP, ROOK, QUEEN};
                for (var promotable : promotablePieces) {
                    moves.add(new PawnPromotion(start, end, new Piece(promotable, board.activePlayer)));
                }
                return moves;
            }

            private Optional<Move> pawnJump(Position start, Board board, Piece piece) {
                var skip = new Position(start.row + piece.color.pawnMove(), start.column);
                var end = new Position(skip.row + piece.color.pawnMove(), skip.column);
                if (board.get(skip).isEmpty() && board.get(end).isEmpty()) {
                    return Optional.of(new PawnJump(start, end, skip));
                }
                return Optional.empty();
            }

            private List<Move> normalMoves(Position position, Board board, Piece piece) {
                var moves = new ArrayList<Move>();
                var nextRow = position.row + piece.color.pawnMove();
                var advance = new Position(nextRow, position.column);
                var captureLeft = new Position(nextRow, position.column - 1);
                var captureRight = new Position(nextRow, position.column + 1);
                if (board.get(advance).isEmpty()) {
                    moves.add(new RegularMove(position, advance));
                }
                var captures = new Position[]{captureLeft, captureRight};
                for (var capture : captures) {
                    if (board.get(capture).isEmpty()) {
                        continue;
                    }
                    var capturePiece = board.get(capture).get();
                    if (capturePiece.color != board.activePlayer) {
                        moves.add(new RegularMove(position, capture));
                    }
                }
                return moves;
            }

            private Optional<Move> enPassant(Position position, Board board, Piece piece) {
                // TODO: implement
                return Optional.empty();
            }
        },
        KNIGHT {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                throw new IllegalStateException("Not Implemented");
            }
        },
        BISHOP {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                throw new IllegalStateException("Not Implemented");
            }
        },
        ROOK {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                throw new IllegalStateException("Not Implemented");
            }
        },
        QUEEN {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                throw new IllegalStateException("Not Implemented");
            }
        },
        KING {
            @Override
            List<Move> possibleMoves(Position position, Board board) {
                // TODO: implement
                throw new IllegalStateException("Not Implemented");
            }
        };

        // The moves which can be made, including those that the king moves into check
        abstract List<Move> possibleMoves(Position position, Board board);
    }

    public final Type type;
    public final Color color;

    private Piece() {
        throw new IllegalStateException("Disable default constructor");
    }

    Piece(Type type, Color color) {
        this.type = type;
        this.color = color;
    }
}
