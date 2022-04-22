import backend.Game;
import backend.Move;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FenCastlingTest {
    static List<Move> getCastlingMoves(List<Move> moves) {
        var castlingMoves = new ArrayList<Move>();
        for (var move : moves) {
            try {
                var c = Class.forName("backend.Castling");
                if (move.getClass() == c) {
                    castlingMoves.add(move);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Reflection failed: " + e);
            }
        }
        return castlingMoves;
    }

    @Test
    void canCastleBothSides() {
        var state = new Game("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void cannotCastleInCheck() {
        var state = new Game("r3k2r/4q3/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertTrue(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheck_1() {
        var state = new Game("r3k2r/5q2/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheck_2() {
        var state = new Game("r3k2r/6q1/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheck_1() {
        var state = new Game("r3k2r/3q4/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheck_2() {
        var state = new Game("r3k2r/2q5/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotCastleEitherSidesTransitCheck() {
        var state = new Game("r3k2r/2q3r1/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttacked_1() {
        var state = new Game("4k3/r7/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttacked_2() {
        var state = new Game("4k3/1r6/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttacked_3() {
        var state = new Game("4k3/7r/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }
}
