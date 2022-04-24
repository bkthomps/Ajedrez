package frontend;

import backend.*;
import bot.BotTurn;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
    static final int ROW_COUNT = 8;
    static final int COLUMN_COUNT = 8;
    private static final Color DARK_BROWN = Color.rgb(160, 80, 0);
    private static final Color LIGHT_BROWN = Color.rgb(200, 100, 0);
    private static final Color DARK_GREEN = Color.rgb(0, 100, 40);
    private static final Color LIGHT_GREEN = Color.rgb(0, 140, 50);

    private Game game;
    private State state;
    private Position moveStart;

    @FXML
    private GridPane board;

    void setPlayerData(PlayerData player, SceneSize size) {
        game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        state = game.generateMoves();
        paintBoard(size, List.of());
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        if (state.isTerminal()) {
            return;
        }
        var scene = ((Node) event.getSource()).getScene();
        var size = new SceneSize(scene);
        int column = (int) (event.getX() / size.width);
        int row = (int) (event.getY() / size.height);
        if (moveStart == null) {
            moveStart = new Position(row, column);
            var endPositions = new ArrayList<Position>();
            for (var move : state.moves()) {
                if (move.start.equals(moveStart)) {
                    endPositions.add(move.end);
                }
            }
            if (endPositions.isEmpty()) {
                moveStart = null;
            }
            paintBoard(size, endPositions);
            return;
        }
        var possibleMoves = new ArrayList<Move>();
        var promotions = new ArrayList<Piece.Type>();
        var end = new Position(row, column);
        for (var move : state.moves()) {
            if (move.start.equals(moveStart) && move.end.equals(end)) {
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
            state = BotTurn.perform(game);
            if (state.isTerminal()) {
                paintBoard(size, List.of());
                if (state.isCheckmate()) {
                    var message = "You have won the game due to " + state.terminalMessage();
                    var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
                    alert.showAndWait();
                } else if (state.isTie()) {
                    var message = "You have tied the game due to " + state.terminalMessage();
                    var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
                    alert.showAndWait();
                }
                return;
            }
            paintBoard(size, List.of());
            state = game.generateMoves();
            if (state.isTerminal()) {
                paintBoard(size, List.of());
                if (state.isCheckmate()) {
                    var message = "You have lost the game due to " + state.terminalMessage();
                    var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
                    alert.showAndWait();
                } else if (state.isTie()) {
                    var message = "You have tied the game due to " + state.terminalMessage();
                    var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
                    alert.showAndWait();
                }
                return;
            }
        }
        moveStart = null;
        paintBoard(size, List.of());
    }

    private void paintBoard(SceneSize size, List<Position> endPositions) {
        board.getChildren().clear();
        var squares = game.getBoard();
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                var r = new Rectangle(size.width, size.height);
                var background = getBackground(i, j, endPositions);
                r.setFill(background);
                var piece = squares[i][j];
                if (piece != null) {
                    var image = getPieceImage(piece, background);
                    r.setFill(image);
                }
                board.add(r, j, i);
            }
        }
    }

    private Color getBackground(int i, int j, List<Position> endPositions) {
        var background = (i + j) % 2 == 0 ? LIGHT_BROWN : DARK_BROWN;
        for (var end : endPositions) {
            if (end.row == i && end.column == j) {
                background = (i + j) % 2 == 0 ? LIGHT_GREEN : DARK_GREEN;
                break;
            }
        }
        return background;
    }

    private ImagePattern getPieceImage(Piece piece, Color background) {
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
        return new ImagePattern(outputImage);
    }
}
