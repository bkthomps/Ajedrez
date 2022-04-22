import backend.Color;
import backend.Game;
import backend.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecursiveDepthTest {
    private static int getMovesCount(Game game, int depth) {
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
    void startDepth_1() {
        var game = new Game(Color.WHITE);
        assertEquals(20, getMovesCount(game, 1));
    }

    @Test
    void startDepth_2() {
        var game = new Game(Color.WHITE);
        assertEquals(400, getMovesCount(game, 2));
    }

    @Test
    void startDepth_3() {
        var game = new Game(Color.WHITE);
        assertEquals(8902, getMovesCount(game, 3));
    }

    @Test
    void startDepth_4() {
        var game = new Game(Color.WHITE);
        assertEquals(197_281, getMovesCount(game, 4));
    }

    @Test
    void startDepth_5() {
        var game = new Game(Color.WHITE);
        assertEquals(4_865_609, getMovesCount(game, 5));
    }

    private static final String testPosition = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";

    @Test
    void midGameDepth_1() {
        var game = new Game(testPosition);
        assertEquals(44, getMovesCount(game, 1));
    }

    @Test
    void midGameDepth_2() {
        var game = new Game(testPosition);
        assertEquals(1486, getMovesCount(game, 2));
    }

    @Test
    void midGameDepth_3() {
        var game = new Game(testPosition);
        assertEquals(62_379, getMovesCount(game, 3));
    }

    @Test
    void midGameDepth_4() {
        var game = new Game(testPosition);
        assertEquals(2_103_487, getMovesCount(game, 4));
    }

    @Test
    void midGameDepth_5() {
        var game = new Game(testPosition);
        assertEquals(89_941_194, getMovesCount(game, 5));
    }
}
