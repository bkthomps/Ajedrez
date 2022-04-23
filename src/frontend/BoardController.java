package frontend;

import backend.Game;
import backend.State;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardController {
    private static final int ROW_COUNT = 8;
    private static final int COLUMN_COUNT = 8;

    private static final Color DARK_BROWN = Color.rgb(160, 80, 0);
    private static final Color LIGHT_BROWN = Color.rgb(200, 100, 0);

    private PlayerData player;
    private Game game;
    private State state;

    @FXML
    private GridPane board;

    void setPlayerData(PlayerData player) {
        this.player = player;
        this.game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        this.state = this.game.generateMoves();
        paintBoard(player.color);
        // TODO: only works for one player and start white at the moment
    }

    private void paintBoard(backend.Color activePlayer) {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < ROW_COUNT; j++) {
                // TODO: size should come from a calculation, not hard-coded
                int size = 600 / 8;
                var r = new Rectangle(size, size);
                r.setFill((i + j) % 2 == 0 ? LIGHT_BROWN : DARK_BROWN);
                board.add(r, i, j);
            }
        }
    }
}
