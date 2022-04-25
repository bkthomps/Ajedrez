package backend;

import java.util.List;

public final class State {
    enum Type {
        CHECKMATE,
        STALEMATE,
        DRAW_INSUFFICIENT_MATING,
        DRAW_THREEFOLD_REPETITION,
        DRAW_FIFTY_MOVE_RULE,
        CHECK,
        NORMAL
    }

    private final Type stateType;
    private final List<Move> moves;

    State(Type stateType, List<Move> moves) {
        this.stateType = stateType;
        this.moves = moves;
    }

    public boolean isTerminal() {
        return stateType != Type.CHECK && stateType != Type.NORMAL;
    }

    public boolean isCheck() {
        if (isTerminal()) {
            throw new IllegalStateException("Must not be in a terminal state.");
        }
        return stateType == Type.CHECK;
    }

    public List<Move> moves() {
        if (isTerminal()) {
            throw new IllegalStateException("Must not be in a terminal state.");
        }
        return moves;
    }

    public boolean isCheckmate() {
        if (!isTerminal()) {
            throw new IllegalStateException("Must be in a terminal state.");
        }
        return stateType == Type.CHECKMATE;
    }

    public boolean isTie() {
        return !isCheckmate();
    }

    public String terminalMessage() {
        return switch (stateType) {
            case CHECKMATE -> "checkmate";
            case STALEMATE -> "stalemate";
            case DRAW_INSUFFICIENT_MATING -> "insufficient mating material";
            case DRAW_THREEFOLD_REPETITION -> "threefold repetition of the board";
            case DRAW_FIFTY_MOVE_RULE -> "fifty moves without captures or pawn movement";
            default -> throw new IllegalStateException("Must be in a terminal state.");
        };
    }
}
