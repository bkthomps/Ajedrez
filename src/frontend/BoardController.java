package frontend;

import backend.Game;
import backend.State;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
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
        var squares = game.getBoard();
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                // TODO: size should come from a calculation, not hard-coded
                int size = 600 / 8;
                var r = new Rectangle(size, size);
                var background = (i + j) % 2 == 0 ? LIGHT_BROWN : DARK_BROWN;
                r.setFill(background);
                var piece = squares[i][j];
                if (piece != null) {
                    var imageName = piece.color + "_" + piece.type + ".png";
                    var image = new Image(imageName);
                    int w = (int) image.getWidth();
                    int h = (int) image.getHeight();
                    var outputImage = new WritableImage(w, h);
                    var writer = outputImage.getPixelWriter();
                    var reader = image.getPixelReader();
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            var oldPixel = reader.getColor(x, y);
                            var pixel = reader.getArgb(x, y) == 0 ? background : oldPixel;
                            writer.setColor(x, y, pixel);
                        }
                    }
                    var pattern = new ImagePattern(outputImage);
                    r.setFill(pattern);
                }
                board.add(r, j, i);
            }
        }
    }
}
