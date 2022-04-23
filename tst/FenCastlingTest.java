import backend.Game;
import backend.Move;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FenCastlingTest {
    private static List<Move> getCastlingMoves(List<Move> moves) {
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
    void singleCastleKnightCheck() {
        var state = new Game("r3k2r/3N4/8/8/8/8/8/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void canCastleBothSidesWhite() {
        var state = new Game("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void cannotCastleInCheckWhite() {
        var state = new Game("r3k2r/4q3/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertTrue(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheckWhite_1() {
        var state = new Game("r3k2r/5q2/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheckWhite_2() {
        var state = new Game("r3k2r/6q1/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheckWhite_1() {
        var state = new Game("r3k2r/3q4/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheckWhite_2() {
        var state = new Game("r3k2r/2q5/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotCastleEitherSidesTransitCheckWhite() {
        var state = new Game("r3k2r/2q3r1/8/8/8/8/8/R3K2R w KQkq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedWhite_1() {
        var state = new Game("4k3/r7/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedWhite_2() {
        var state = new Game("4k3/1r6/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedWhite_3() {
        var state = new Game("4k3/7r/8/8/8/8/8/R3K2R w KQ - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void cannotCastleAlreadyMovedWhite() {
        var state = new Game("4k3/8/8/8/8/8/8/R3K2R w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void canCastleBothSidesBlack() {
        var state = new Game("r3k2r/8/8/8/8/8/8/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void cannotCastleInCheckBlack() {
        var state = new Game("r3k2r/8/8/8/8/8/4R3/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertTrue(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheckBlack_1() {
        var state = new Game("r3k2r/8/8/8/8/8/5R2/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotShortCastleTransitCheckBlack_2() {
        var state = new Game("r3k2r/8/8/8/8/8/6R1/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheckBlack_1() {
        var state = new Game("r3k2r/8/8/8/8/8/3R4/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotLongCastleTransitCheckBlack_2() {
        var state = new Game("r3k2r/8/8/8/8/8/2R5/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(1, castlingMoves.size());
    }

    @Test
    void cannotCastleEitherSidesTransitCheckBlack() {
        var state = new Game("r3k2r/8/8/8/8/8/2R3R1/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedBlack_1() {
        var state = new Game("r3k2r/8/8/8/8/8/R7/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedBlack_2() {
        var state = new Game("r3k2r/8/8/8/8/8/7R/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void canCastleIfRooksAttackedBlack_3() {
        var state = new Game("r3k2r/8/8/8/8/8/R6R/4K3 b kq - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(2, castlingMoves.size());
    }

    @Test
    void cannotCastleAlreadyMovedBlack() {
        var state = new Game("r3k2r/8/8/8/8/8/8/4K3 b - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
        assertFalse(state.isCheck());
        var castlingMoves = getCastlingMoves(state.moves());
        assertEquals(0, castlingMoves.size());
    }
}
