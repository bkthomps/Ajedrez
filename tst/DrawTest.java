import backend.Game;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrawTest {
    @Test
    void no_draw_single_queen() {
        var state = new Game("4k3/8/8/8/8/8/8/Q3K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_single_rook() {
        var state = new Game("4k3/8/8/8/8/8/8/R3K3 w Q - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_double_bishop() {
        var state = new Game("4k3/8/8/8/8/8/8/BB2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_double_single_bishop() {
        var state = new Game("b3k3/8/8/8/8/8/8/1B2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_double_single_knight() {
        var state = new Game("n3k3/8/8/8/8/8/8/N3K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_triple_knight() {
        var state = new Game("4k3/8/8/8/8/8/N7/NN2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_knight_bishop() {
        var state = new Game("4k3/8/8/8/8/8/8/BN2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_knight_enemy_bishop() {
        var state = new Game("b3k3/8/8/8/8/8/8/1N2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void no_draw_double_knight() {
        var state = new Game("4k3/8/8/8/8/8/8/NN2K3 w - - 0 1").generateMoves();
        assertFalse(state.isTerminal());
    }

    @Test
    void draw_single_bishop() {
        var state = new Game("4k3/8/8/8/8/8/8/B3K3 w - - 0 1").generateMoves();
        assertTrue(state.isTerminal());
    }

    @Test
    void draw_single_knight() {
        var state = new Game("4k3/8/8/8/8/8/8/1N2K3 w - - 0 1").generateMoves();
        assertTrue(state.isTerminal());
    }
}
