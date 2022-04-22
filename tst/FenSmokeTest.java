import backend.Color;
import backend.Game;
import backend.Move;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FenSmokeTest {
    static int moveCount(String fen) {
        return new Game(fen).generateMoves().moves().size();
    }

    static void assertPromotionPieces(int count, List<Move> moves) {
        int knightCount = 0;
        int bishopCount = 0;
        int rookCount = 0;
        int queenCount = 0;
        for (var move : moves) {
            var pieceType = move.promotionPieceType();
            if (pieceType.isEmpty()) {
                continue;
            }
            switch (pieceType.get()) {
                case KNIGHT -> knightCount++;
                case BISHOP -> bishopCount++;
                case ROOK -> rookCount++;
                case QUEEN -> queenCount++;
            }
        }
        assertEquals(count, knightCount);
        assertEquals(count, bishopCount);
        assertEquals(count, rookCount);
        assertEquals(count, queenCount);
    }

    static void assertCheckmate(String fen) {
        var state = new Game(fen).generateMoves();
        assertTrue(state.isTerminal());
        assertTrue(state.isCheckmate());
    }

    static void assertStalemate(String fen) {
        var state = new Game(fen).generateMoves();
        assertTrue(state.isTerminal());
        assertTrue(state.isTie());
        assertTrue(state.terminalMessage().contains("stalemate"));
    }

    @Test
    void startMoves() {
        var game = new Game(Color.WHITE);
        var state = game.generateMoves();
        assertFalse(state.isTerminal());
        var moves = state.moves();
        assertEquals(20, moves.size());
    }

    @Test
    void pawnPromotionWhite() {
        var moves = new Game("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1").generateMoves().moves();
        assertEquals(9, moves.size());
        assertPromotionPieces(1, moves);
        moves = new Game("2n1k3/1P6/8/8/8/8/8/4K3 w - - 0 1").generateMoves().moves();
        assertEquals(13, moves.size());
        assertPromotionPieces(2, moves);
        moves = new Game("n3k3/1P6/8/8/8/8/8/4K3 w - - 0 1").generateMoves().moves();
        assertEquals(13, moves.size());
        assertPromotionPieces(2, moves);
        moves = new Game("n1n1k3/1P6/8/8/8/8/8/4K3 w - - 0 1").generateMoves().moves();
        assertEquals(17, moves.size());
        assertPromotionPieces(3, moves);
    }

    @Test
    void pawnPromotionBlack() {
        var moves = new Game("4k3/8/8/8/8/8/1p6/4K3 b - - 0 1").generateMoves().moves();
        assertEquals(9, moves.size());
        assertPromotionPieces(1, moves);
        moves = new Game("4k3/8/8/8/8/8/1p6/2N1K3 b - - 0 1").generateMoves().moves();
        assertEquals(13, moves.size());
        assertPromotionPieces(2, moves);
        moves = new Game("4k3/8/8/8/8/8/1p6/N3K3 b - - 0 1").generateMoves().moves();
        assertEquals(13, moves.size());
        assertPromotionPieces(2, moves);
        moves = new Game("4k3/8/8/8/8/8/1p6/N1N1K3 b - - 0 1").generateMoves().moves();
        assertEquals(17, moves.size());
        assertPromotionPieces(3, moves);
    }

    @Test
    void enPassantWhite() {
        assertEquals(6, moveCount("4k3/8/8/pP6/8/8/8/4K3 w - a6 0 1"));
        assertEquals(7, moveCount("4k3/8/8/2PpP3/8/8/8/4K3 w - d6 0 1"));
    }

    @Test
    void enPassantBlack() {
        assertEquals(6, moveCount("4k3/8/8/8/6pP/8/8/4K3 b - h3 0 1"));
        assertEquals(7, moveCount("4k3/8/8/8/4pPp1/8/8/4K3 b - f3 0 1"));
    }

    @Test
    void checkmate() {
        assertCheckmate("8/8/8/6p1/8/4k3/8/2q1K3 w - - 6 17");
        assertCheckmate("r4r2/1p2Nppk/p7/3p4/P2qn2R/8/1P4PP/5R1K b - - 0 3");
        assertCheckmate("6kR/6P1/5K2/8/8/8/8/8 b - - 1 1");
        assertCheckmate("7k/pp2N2p/8/4p3/P7/1P1PPn2/2P4r/R6K w - - 0 4");
        assertCheckmate("2R5/pp3ppk/7p/3p4/8/P7/2P2PPP/1q4K1 w - - 0 3");
        assertCheckmate("4k3/8/2B2Q2/8/8/8/8/6K1 b - - 1 1");
        assertCheckmate("5rk1/7B/8/6N1/8/8/1B6/6K1 b - - 1 1");
        assertCheckmate("5rk1/6RR/8/8/8/8/8/6K1 b - - 5 3");
        assertCheckmate("2kr2r1/3n1p2/B1p4p/8/1P1Pn1b1/5N2/P4PPB/5RK1 b - - 0 3");
        assertCheckmate("5brk/5N1p/8/2n1P3/8/8/3B4/2K3R1 b - - 0 2");
    }

    @Test
    void stalemate() {
        assertStalemate("3k4/8/2QP4/P4p2/5P2/8/7P/6K1 b - - 0 3");
        assertStalemate("4b2k/5pp1/6p1/6K1/2r5/8/8/8 w - - 0 6");
        assertStalemate("8/4k3/8/1p2p2p/1P2Pn1P/5Pq1/4r3/7K w - - 0 5");
        assertStalemate("8/8/8/5k2/5p2/5K2/1r6/8 w - - 0 3");
        assertStalemate("8/3b4/p1p5/kp6/4p3/8/5q2/7K w - - 0 4");
        assertStalemate("3k4/3p4/2pK1p2/2P5/8/8/8/8 w - - 0 5");
        assertStalemate("6R1/8/8/5P2/7p/7k/8/Q6K b - - 0 4");
        assertStalemate("k7/4q1b1/1p6/p7/5p2/2n2P2/7r/2K5 w - - 0 5");
        assertStalemate("8/6k1/p2pn2p/6p1/4qpP1/7K/7P/8 w - - 0 3");
        assertStalemate("8/1r6/8/8/6p1/6k1/6p1/6K1 w - - 0 5");
    }

    @Test
    void smoke8pieces() {
        assertEquals(4, moveCount("8/2p3p1/8/7P/K5p1/1p3r2/1k6/8 w - - 0 1"));
        assertEquals(5, moveCount("k4n2/2Kp4/3p4/8/6P1/8/4pp2/8 w - - 0 1"));
        assertEquals(2, moveCount("8/8/8/5p1p/b6P/3p4/r1k5/4K3 w - - 0 1"));
        assertEquals(20, moveCount("1k6/8/6pK/P4b2/8/8/5Pp1/3R4 w - - 0 1"));
        assertEquals(21, moveCount("1K6/2N1p3/3k3P/P7/8/8/1P6/1R6 w - - 0 1"));
        assertEquals(26, moveCount("8/6N1/8/8/2KbP3/7R/5P1p/k7 w - - 0 1"));
        assertEquals(14, moveCount("8/3qb3/B7/r7/8/4KP1k/2P5/8 w - - 0 1"));
        assertEquals(19, moveCount("6r1/K2p4/2R5/PP6/8/8/3p4/5k2 w - - 0 1"));
        assertEquals(28, moveCount("2K1Q3/7k/8/3b4/N4p2/1p6/2P5/8 w - - 0 1"));
        assertEquals(13, moveCount("8/4r3/2K5/1p6/3P2n1/7P/k5B1/8 w - - 0 1"));
        assertEquals(15, moveCount("8/6K1/8/6P1/k6p/2PP4/6N1/r7 w - - 0 1"));
        assertEquals(16, moveCount("4q3/1K6/8/1P6/pn3B2/3P3k/8/8 w - - 0 1"));
        assertEquals(31, moveCount("N1b2k2/8/2P5/2q5/8/6R1/2R5/5K2 w - - 0 1"));
        assertEquals(28, moveCount("2K4b/8/8/2P5/8/p1P1p3/1k6/5Q2 w - - 0 1"));
        assertEquals(16, moveCount("5b2/2K5/2P4P/8/q2P4/5k2/3B4/8 w - - 0 1"));
        assertEquals(30, moveCount("4Q3/2k5/n3KR1p/8/8/8/4Pp2/8 w - - 0 1"));
        assertEquals(21, moveCount("7K/3pk1P1/1R6/P3p3/8/5p2/8/8 w - - 0 1"));
        assertEquals(11, moveCount("8/k6B/2r5/2p5/K7/2P5/2pp4/8 w - - 0 1"));
        assertEquals(29, moveCount("6K1/1B6/1P6/6n1/6k1/3nQ3/5q2/8 w - - 0 1"));
        assertEquals(14, moveCount("8/2K1p3/2n1p3/5N2/8/1rk4B/8/8 w - - 0 1"));
    }

    @Test
    void smoke16pieces() {
        assertEquals(20, moveCount("1r2R3/2P2n2/2p4n/3ppb2/p6K/1k1P3P/1p5p/8 w - - 0 1"));
        assertEquals(34, moveCount("8/2p1pNK1/4k1p1/1q6/pR6/1P1pBp1P/6p1/N7 w - - 0 1"));
        assertEquals(26, moveCount("1BR4r/8/k3K3/1pp5/Pq3r1n/1P4P1/B3p3/1N6 w - - 0 1"));
        assertEquals(36, moveCount("8/1P2nr2/N5Q1/4P2R/4K2p/3rp2p/kq3p2/3N4 w - - 0 1"));
        assertEquals(19, moveCount("8/3Nb2p/3P2P1/5p2/2bK4/1P6/PpPk1p2/2Nr4 w - - 0 1"));
        assertEquals(42, moveCount("8/3NR3/n7/3P2PP/2P2p2/b1p1N3/1PB5/1K2B2k w - - 0 1"));
        assertEquals(15, moveCount("8/7K/P3PpP1/2k1p3/7P/1bpP2p1/p2r4/1RN5 w - - 0 1"));
        assertEquals(50, moveCount("1R6/2kp4/1pB3p1/PP6/p1P5/2Q3R1/2K2Pnq/8 w - - 0 1"));
        assertEquals(31, moveCount("1K6/P4r1p/NP2r3/5p2/Bb1k3p/6p1/4P3/5R1N w - - 0 1"));
        assertEquals(34, moveCount("4R2n/1PPp4/6q1/1kPP3P/p4K2/2P3p1/8/6Br w - - 0 1"));
        assertEquals(42, moveCount("8/3R2q1/4kN1b/4p3/1p1p2n1/P7/b2pK2P/R1Q5 w - - 0 1"));
        assertEquals(35, moveCount("3B3k/N7/B1P2pP1/2K5/P2P4/1P2p3/3n2p1/5R1b w - - 0 1"));
        assertEquals(45, moveCount("1r6/2N1B3/RnB5/5KP1/P2p1n2/2r5/3pkP1Q/8 w - - 0 1"));
        assertEquals(28, moveCount("5N2/P4Pb1/1p2P2P/6p1/2p3nk/5p2/1R5p/1KB5 w - - 0 1"));
        assertEquals(45, moveCount("2Nn1N2/1P2r2P/8/1K5p/1P4k1/6b1/2qPrQ2/2R5 w - - 0 1"));
        assertEquals(44, moveCount("4B3/5Q2/2Rp1p2/P3k1NN/6P1/4Pbp1/1K2n1p1/8 w - - 0 1"));
        assertEquals(18, moveCount("3k4/p4r2/5b1P/1K3p1p/8/1RP5/1nP1P1P1/1R1q4 w - - 0 1"));
        assertEquals(37, moveCount("3k4/1PRN4/4P2R/6b1/1P1n4/3pnPN1/2bP1K2/8 w - - 0 1"));
        assertEquals(31, moveCount("8/4N1P1/p7/5P1P/p1rk4/1P3K1p/4p1nb/1q2Q3 w - - 0 1"));
        assertEquals(41, moveCount("8/2p3kn/8/2b1K1P1/1q5R/Q1p1P2p/3P3R/1b1B4 w - - 0 1"));
    }

    @Test
    void smoke24pieces() {
        assertEquals(44, moveCount("3B1Q1b/1P2p1pp/n4PP1/2r5/5pPr/qk1BPP2/p2K2N1/R2R4 w - - 0 1"));
        assertEquals(39, moveCount("1b3Qb1/1RppRp1p/P1P5/2rp1n2/1N1kP3/K2P1B1q/2pP4/2N5 w - - 0 1"));
        assertEquals(47, moveCount("3q4/R1p5/2N1pP1r/B1pnpB1P/1p1p1P1Q/1kP5/1p1p2P1/R6K w - - 0 1"));
        assertEquals(43, moveCount("6RR/3nPpB1/p3N1B1/P1p3r1/P2P2k1/2p1K1P1/1bpP1P1P/7r w - - 0 1"));
        assertEquals(42, moveCount("2Rq4/rN1Pb3/1pb5/6k1/1PPrB3/pPn1p2n/N3p2K/2R1Q1B1 w - - 0 1"));
        assertEquals(46, moveCount("3B4/P2rp3/2P2n1K/Pb1RPQ1p/1Pp4b/NR2Pn2/1N3p1k/6r1 w - - 0 1"));
        assertEquals(44, moveCount("2BN3B/1q4Pp/2ppn2R/1pPb1N2/2kp4/4Qr1P/2PK1P1R/6b1 w - - 0 1"));
        assertEquals(46, moveCount("8/P4bp1/KR4pP/NR2Q1p1/BN4p1/1P1P1PP1/P2pr1P1/6nk w - - 0 1"));
        assertEquals(34, moveCount("K3BN1k/2pP3N/n3Qp1P/2pP4/3P1ppp/2P4P/3Bpn1r/2q5 w - - 0 1"));
        assertEquals(43, moveCount("2r4b/2kPnpB1/2B1pP1p/P3p2P/1qPrR1p1/2p5/1QN3p1/6K1 w - - 0 1"));
    }

    @Test
    void smoke32pieces() {
        assertEquals(48, moveCount("2K5/2Q2PPr/1R1P2bp/pnp1NpN1/k1PBRPp1/b2r1P2/p1PPBpp1/2n1q3 w - - 0 1"));
        assertEquals(37, moveCount("1b5N/kp3np1/1n1PrQP1/1B2B3/2P1KppP/PprP2Rb/p1RPpP1p/5Nq1 w - - 0 1"));
        assertEquals(39, moveCount("n7/B2r1p1p/1P1b1K1p/NPPkr3/1P2ppPP/1PppPp2/R4Qb1/1NnBq2R w - - 0 1"));
        assertEquals(46, moveCount("8/pppbpPq1/2PRP2p/1P2PNPp/k2r2pP/B2QbR2/nK3npP/3N1Br1 w - - 0 1"));
        assertEquals(51, moveCount("Q5b1/q2rN1BP/pP2pPP1/pp5P/B1r2NP1/p1b1PP1n/k1p1ppRK/4nR2 w - - 0 1"));
        assertEquals(53, moveCount("3K4/pPP1nppP/R1pn2rq/NP1k2pB/Rp3B2/1PPr2b1/p1N1PPp1/Q4b2 w - - 0 1"));
        assertEquals(34, moveCount("1N1RBn2/P1bp1bBp/1k2P1rP/pP1R1pr1/2PK1p2/1pP2p1n/2pNP2P/3q3Q w - - 0 1"));
        assertEquals(33, moveCount("n1b4r/3R1KpP/P1pRPNpB/k2pp3/3P2pP/1rpP2bB/1pNPq1P1/6nQ w - - 0 1"));
        assertEquals(64, moveCount("n1B3b1/P3NPpP/1PKPP2b/p1ppqPk1/r2R1pN1/p7/PB1Qp2p/2R2n1r w - - 0 1"));
        assertEquals(32, moveCount("2N3BN/B1Q2p1P/RK1P3n/3PPpr1/2P2rk1/2pP1pp1/1ppP1pP1/1Rn1bq1b w - - 0 1"));
    }
}
