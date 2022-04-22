import backend.Game;
import backend.Move;
import backend.State;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecursiveDepthTest {
    private static List<Move> getMoveCountOf(List<Move> moves, String clazz) {
        var specialMoves = new ArrayList<Move>();
        for (var move : moves) {
            try {
                var c = Class.forName(clazz);
                if (move.getClass() == c) {
                    specialMoves.add(move);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Reflection failed: " + e);
            }
        }
        return specialMoves;
    }

    private static void assertMoves(int count, int promotions, int castles, int enPassant, String fen, int depth) {
        var moves = getMoves(fen, depth);
        int promotionCount = 0;
        for (var move : moves) {
            if (move.promotionPieceType().isPresent()) {
                promotionCount++;
            }
        }
        assertEquals(promotions, promotionCount);
        assertEquals(castles, getMoveCountOf(moves, "backend.Castling").size());
        assertEquals(enPassant, getMoveCountOf(moves, "backend.EnPassant").size());
        assertEquals(count, moves.size());
    }

    private static List<Move> getMoves(String fen, int depth) {
        var game = new Game(fen);
        return getMoves(game, game.generateMoves(), depth - 1);
    }

    private static List<Move> getMoves(Game game, State state, int depth) {
        if (state.isTerminal()) {
            return List.of();
        }
        if (depth == 0) {
            return state.moves();
        }
        var moves = new ArrayList<Move>();
        for (var move : state.moves()) {
            move.perform();
            moves.addAll(getMoves(game, game.generateMoves(), depth - 1));
            move.undo();
        }
        return moves;
    }

    @Test
    void position_1() {
        var fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        var moves = new int[]{20, 400, 8_902, 197_281, 4_865_609};
        var promotions = new int[]{0, 0, 0, 0, 0};
        var castles = new int[]{0, 0, 0, 0, 0};
        var enPassant = new int[]{0, 0, 0, 0, 258};
        for (int i = 0; i < moves.length; i++) {
            assertMoves(moves[i], promotions[i], castles[i], enPassant[i], fen, i + 1);
        }
    }

    @Test
    void position_2() {
        var fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        var moves = new int[]{48, 2_039, 97_862, 4_085_603};
        var promotions = new int[]{0, 0, 0, 15_172};
        var castles = new int[]{2, 91, 3_162, 128013};
        var enPassant = new int[]{0, 1, 45, 1929};
        for (int i = 0; i < moves.length; i++) {
            assertMoves(moves[i], promotions[i], castles[i], enPassant[i], fen, i + 1);
        }
    }

    @Test
    void position_3() {
        var fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        var moves = new int[]{14, 191, 2_812, 43_238, 674_624};
        var promotions = new int[]{0, 0, 0, 0, 0};
        var castles = new int[]{0, 0, 0, 0, 0};
        var enPassant = new int[]{0, 0, 2, 123, 1165};
        for (int i = 0; i < moves.length; i++) {
            assertMoves(moves[i], promotions[i], castles[i], enPassant[i], fen, i + 1);
        }
    }

    @Test
    void position_4() {
        var fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        var moves = new int[]{6, 264, 9_467, 422_333};
        var promotions = new int[]{0, 48, 120, 60_032};
        var castles = new int[]{0, 6, 0, 7_795};
        var enPassant = new int[]{0, 0, 4, 0};
        for (int i = 0; i < moves.length; i++) {
            assertMoves(moves[i], promotions[i], castles[i], enPassant[i], fen, i + 1);
        }
    }

    @Test
    void position_4_mirrored() {
        var fen = "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1";
        var moves = new int[]{6, 264, 9_467, 422_333};
        var promotions = new int[]{0, 48, 120, 60_032};
        var castles = new int[]{0, 6, 0, 7_795};
        var enPassant = new int[]{0, 0, 4, 0};
        for (int i = 0; i < moves.length; i++) {
            assertMoves(moves[i], promotions[i], castles[i], enPassant[i], fen, i + 1);
        }
    }

    @Test
    void position_5() {
        var fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        assertEquals(44, getMoves(fen, 1).size());
        assertEquals(1_486, getMoves(fen, 2).size());
        assertEquals(62_379, getMoves(fen, 3).size());
        assertEquals(2_103_487, getMoves(fen, 4).size());
    }

    @Test
    void position_6() {
        var fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        assertEquals(46, getMoves(fen, 1).size());
        assertEquals(2_079, getMoves(fen, 2).size());
        assertEquals(89_890, getMoves(fen, 3).size());
        assertEquals(3_894_594, getMoves(fen, 4).size());
    }
}
