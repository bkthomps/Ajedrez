import backend.Game;
import backend.Move;
import backend.State;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecursiveDepthTest {
    private static final class Generate {
        private final String fen;
        private Integer depth;
        private Integer count;
        private Integer promotions;
        private Integer castles;
        private Integer enPassant;

        private Generate(String fen) {
            this.fen = fen;
        }

        private static Generate moves(String fen) {
            return new Generate(fen);
        }

        private Generate depth(int depth) {
            this.depth = depth;
            return this;
        }

        private Generate count(int count) {
            this.count = count;
            return this;
        }

        private Generate promotions(int promotions) {
            this.promotions = promotions;
            return this;
        }

        private Generate castles(int castles) {
            this.castles = castles;
            return this;
        }

        private Generate enPassant(int enPassant) {
            this.enPassant = enPassant;
            return this;
        }

        private void assertCount(int count) {
            if (fen == null || depth == null || this.count != null
                    || promotions != null || castles != null || enPassant != null) {
                throw new IllegalStateException("Specified too many fields or not enough fields");
            }
            assertMoves(depth, fen, count, promotions, castles, enPassant);
        }

        private void assertAll() {
            if (fen == null || depth == null || count == null
                    || promotions == null || castles == null || enPassant == null) {
                throw new IllegalStateException("All fields must be specified");
            }
            assertMoves(depth, fen, count, promotions, castles, enPassant);
        }

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

        private static void assertMoves(int depth, String fen, int count,
                                        Integer promotions, Integer castles, Integer enPassant) {
            var moves = getMoves(fen, depth);
            int promotionCount = 0;
            for (var move : moves) {
                if (move.promotionPieceType().isPresent()) {
                    promotionCount++;
                }
            }
            if (promotions != null) {
                assertEquals(promotions, promotionCount);
            }
            if (castles != null) {
                assertEquals(castles, getMoveCountOf(moves, "backend.Castling").size());
            }
            if (enPassant != null) {
                assertEquals(enPassant, getMoveCountOf(moves, "backend.EnPassant").size());
            }
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
    }

    @Test
    void position_1() {
        var fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        Generate.moves(fen).depth(1).count(20).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(2).count(400).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(3).count(8_902).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(4).count(197_281).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(5).count(4_865_609).promotions(0).castles(0).enPassant(258).assertAll();
    }

    @Test
    void position_2() {
        var fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        Generate.moves(fen).depth(1).count(48).promotions(0).castles(2).enPassant(0).assertAll();
        Generate.moves(fen).depth(2).count(2_039).promotions(0).castles(91).enPassant(1).assertAll();
        Generate.moves(fen).depth(3).count(97_862).promotions(0).castles(3_162).enPassant(45).assertAll();
        Generate.moves(fen).depth(4).count(4_085_603).promotions(15_172).castles(128_013).enPassant(1_929).assertAll();
    }

    @Test
    void position_3() {
        var fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        Generate.moves(fen).depth(1).count(14).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(2).count(191).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(3).count(2_812).promotions(0).castles(0).enPassant(2).assertAll();
        Generate.moves(fen).depth(4).count(43_238).promotions(0).castles(0).enPassant(123).assertAll();
        Generate.moves(fen).depth(5).count(674_624).promotions(0).castles(0).enPassant(1_165).assertAll();
    }

    @Test
    void position_4() {
        var fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        Generate.moves(fen).depth(1).count(6).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(2).count(264).promotions(48).castles(6).enPassant(0).assertAll();
        Generate.moves(fen).depth(3).count(9_467).promotions(120).castles(0).enPassant(4).assertAll();
        Generate.moves(fen).depth(4).count(422_333).promotions(60_032).castles(7_795).enPassant(0).assertAll();
    }

    @Test
    void position_4_mirrored() {
        var fen = "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1";
        Generate.moves(fen).depth(1).count(6).promotions(0).castles(0).enPassant(0).assertAll();
        Generate.moves(fen).depth(2).count(264).promotions(48).castles(6).enPassant(0).assertAll();
        Generate.moves(fen).depth(3).count(9_467).promotions(120).castles(0).enPassant(4).assertAll();
        Generate.moves(fen).depth(4).count(422_333).promotions(60_032).castles(7_795).enPassant(0).assertAll();
    }

    @Test
    void position_5() {
        var fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        Generate.moves(fen).depth(1).assertCount(44);
        Generate.moves(fen).depth(2).assertCount(1_486);
        Generate.moves(fen).depth(3).assertCount(62_379);
        Generate.moves(fen).depth(4).assertCount(2_103_487);
    }

    @Test
    void position_6() {
        var fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        Generate.moves(fen).depth(1).assertCount(46);
        Generate.moves(fen).depth(2).assertCount(2_079);
        Generate.moves(fen).depth(3).assertCount(89_890);
        Generate.moves(fen).depth(4).assertCount(3_894_594);
    }
}
