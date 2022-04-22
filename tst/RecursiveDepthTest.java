import backend.Game;
import backend.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecursiveDepthTest {
    private static int getMovesCount(String fen, int depth) {
        var game = new Game(fen);
        return getMovesCount(game, game.generateMoves(), depth - 1);
    }

    private static int getMovesCount(Game game, State state, int depth) {
        if (state.isTerminal()) {
            return 0;
        }
        if (depth == 0) {
            return state.moves().size();
        }
        int count = 0;
        for (var move : state.moves()) {
            move.perform();
            count += getMovesCount(game, game.generateMoves(), depth - 1);
            move.undo();
        }
        return count;
    }

    @Test
    void position_1() {
        var fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        assertEquals(20, getMovesCount(fen, 1));
        assertEquals(400, getMovesCount(fen, 2));
        assertEquals(8_902, getMovesCount(fen, 3));
        assertEquals(197_281, getMovesCount(fen, 4));
        assertEquals(4_865_609, getMovesCount(fen, 5));
    }

    @Test
    void position_2() {
        var fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        assertEquals(48, getMovesCount(fen, 1));
        assertEquals(2_039, getMovesCount(fen, 2));
        assertEquals(97_862, getMovesCount(fen, 3));
        assertEquals(4_085_603, getMovesCount(fen, 4));
    }

    @Test
    void position_3() {
        var fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        assertEquals(14, getMovesCount(fen, 1));
        assertEquals(191, getMovesCount(fen, 2));
        assertEquals(2_812, getMovesCount(fen, 3));
        assertEquals(43_238, getMovesCount(fen, 4));
        assertEquals(674_624, getMovesCount(fen, 5));
    }

    @Test
    void position_4() {
        var fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        assertEquals(6, getMovesCount(fen, 1));
        assertEquals(264, getMovesCount(fen, 2));
        assertEquals(9_467, getMovesCount(fen, 3));
        assertEquals(422_333, getMovesCount(fen, 4));
    }

    @Test
    void position_4_mirrored() {
        var fen = "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1";
        assertEquals(6, getMovesCount(fen, 1));
        assertEquals(264, getMovesCount(fen, 2));
        assertEquals(9_467, getMovesCount(fen, 3));
        assertEquals(422_333, getMovesCount(fen, 4));
    }

    @Test
    void position_5() {
        var fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        assertEquals(44, getMovesCount(fen, 1));
        assertEquals(1_486, getMovesCount(fen, 2));
        assertEquals(62_379, getMovesCount(fen, 3));
        assertEquals(2_103_487, getMovesCount(fen, 4));
    }

    @Test
    void position_6() {
        var fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        assertEquals(46, getMovesCount(fen, 1));
        assertEquals(2_079, getMovesCount(fen, 2));
        assertEquals(89_890, getMovesCount(fen, 3));
        assertEquals(3_894_594, getMovesCount(fen, 4));
    }
}
