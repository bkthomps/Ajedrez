package frontend;

import backend.*;
import bot.BotTurn;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public final class BoardController {
    private static final int ROW_COUNT = 8;
    private static final int COLUMN_COUNT = 8;

    private static final Color DARK_BROWN = Color.rgb(160, 80, 0);
    private static final Color LIGHT_BROWN = Color.rgb(200, 100, 0);
    private static final Color DARK_GREEN = Color.rgb(0, 100, 40);
    private static final Color LIGHT_GREEN = Color.rgb(0, 140, 50);

    private static PlayerData player;
    private static Game game;
    private static State state;
    private static Position start;
    private static backend.Color activePlayer;

    @FXML
    private GridPane board;

    void setPlayerData(PlayerData player) {
        BoardController.player = player;
        BoardController.game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        BoardController.state = BoardController.game.generateMoves();
        if (BoardController.state.isTerminal()) {
            // TODO: end game
        }
        activePlayer = player.color;
        paintBoard(activePlayer, List.of());
        // TODO: only works for one player and start white at the moment
        // TODO: make it not static
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        // TODO: size should come from a calculation, not hard-coded
        int size = 600 / 8;
        int column = (int) event.getX() / size;
        int row = (int) event.getY() / size;
        if (state == null || state.isTerminal()) {
            return;
        }
        if (start == null) {
            start = new Position(row, column);
            var endPositions = new ArrayList<Position>();
            for (var move : state.moves()) {
                if (move.start.equals(start)) {
                    endPositions.add(move.end);
                }
            }
            if (endPositions.isEmpty()) {
                start = null;
            }
            paintBoard(activePlayer, endPositions);
            return;
        }
        var possibleMoves = new ArrayList<Move>();
        var promotions = new ArrayList<Piece.Type>();
        var end = new Position(row, column);
        for (var move : state.moves()) {
            // TODO: can have more than 1 for promotions, check this
            if (move.start.equals(start) && move.end.equals(end)) {
                possibleMoves.add(move);
                var promotion = move.promotionPieceType();
                promotion.ifPresent(promotions::add);
            }
        }
        Piece.Type promoteTo = null;
        if (!promotions.isEmpty()) {
            var buttons = new ButtonType[promotions.size()];
            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = new ButtonType(promotions.get(i).toString());
            }
            var alert = new Alert(Alert.AlertType.NONE, "What should this pawn be promoted to?", buttons);
            var result = alert.showAndWait();
            var button = result.orElse(buttons[0]);
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].equals(button)) {
                    promoteTo = promotions.get(i);
                }
            }
        }
        for (var move : possibleMoves) {
            var promotion = move.promotionPieceType();
            if (promotion.isPresent() && promotion.get() != promoteTo) {
                continue;
            }
            move.perform();
            state = null;
            var botState = BotTurn.perform(game);
            if (botState.isTerminal()) {
                // TODO: end game
                return;
            }
            paintBoard(activePlayer, List.of());
            if (player.count != Players.ONE_PLAYER) {
                activePlayer = activePlayer.next();
            }
            state = game.generateMoves();
            if (state.isTerminal()) {
                // TODO: end game
                return;
            }
        }
        start = null;
        paintBoard(activePlayer, List.of());
        if (player.count != Players.ONE_PLAYER) {
            activePlayer = activePlayer.next();
        }
    }

    private void paintBoard(backend.Color activePlayer, List<Position> endPositions) {
        var squares = game.getBoard();
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                // TODO: size should come from a calculation, not hard-coded
                int size = 600 / 8;
                var r = new Rectangle(size, size);
                var background = (i + j) % 2 == 0 ? LIGHT_BROWN : DARK_BROWN;
                for (var end : endPositions) {
                    if (end.row == i && end.column == j) {
                        background = (i + j) % 2 == 0 ? LIGHT_GREEN : DARK_GREEN;
                        break;
                    }
                }
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
