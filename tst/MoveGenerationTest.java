import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import backend.Color;
import backend.Game;

class MoveGenerationTest {
    @Test
    void startMoves() {
        var game = new Game(Color.WHITE);
        var state = game.generateMoves();
        assertFalse(state.isTerminal());
        var moves = state.moves();
        assertEquals(20, moves.size());
    }
}
