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
    private boolean displayWhite;
    private Players players;

    @FXML
    private GridPane board;

    void setPlayerData(PlayerData player, SceneSize size) {
        game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        displayWhite = (player.color == backend.Color.WHITE);
        players = player.count;
        if (player.color == backend.Color.BLACK && player.count == Players.ONE_PLAYER) {
            state = BotTurn.perform(game);
            paintBoard(game, size);
            if (state.isTerminal()) {
                alertUserTerminatedGame(state, "You have won");
                return;
            }
        }
        state = game.generateMoves();
        paintBoard(game, size);
        if (state.isTerminal()) {
            alertUserTerminatedGame(state, getTerminalMessage());
        }
    }

    private String getTerminalMessage() {
        if (players == Players.ONE_PLAYER) {
            return "You have lost";
        }
        if (displayWhite) {
            return "Black has won";
        }
        return "White has won";
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        if (state.isTerminal()) {
            return;
        }
        var scene = ((Node) event.getSource()).getScene();
        var size = new SceneSize(scene);
        var clickPosition = getClickPosition(size, event);
        if (moveStart == null) {
            var endPositions = getEndPositions(state, clickPosition);
            if (endPositions.isEmpty()) {
                moveStart = null;
                return;
            }
            moveStart = clickPosition;
            paintBoard(game, size, endPositions);
            return;
        }
        var possibleMoves = getPossibleMoves(state, moveStart, clickPosition);
        moveStart = null;
        if (possibleMoves.isEmpty()) {
            paintBoard(game, size);
            return;
        }
        var promotions = getPiecePromotions(possibleMoves);
        Piece.Type promoteTo = null;
        if (!promotions.isEmpty()) {
            promoteTo = getPromotionPieceFromUser(promotions);
        }
        var move = getSelectedMove(possibleMoves, promoteTo);
        move.perform();
        if (players == Players.TWO_PLAYERS) {
            displayWhite = !displayWhite;
        }
        paintBoard(game, size);
        if (players == Players.ONE_PLAYER) {
            state = BotTurn.perform(game);
            paintBoard(game, size);
            if (state.isTerminal()) {
                alertUserTerminatedGame(state, "You have won");
                return;
            }
        }
        state = game.generateMoves();
        if (state.isTerminal()) {
            alertUserTerminatedGame(state, getTerminalMessage());
        }
    }

    private Position getClickPosition(SceneSize size, MouseEvent event) {
        int column = (int) (event.getX() / size.width);
        int row = (int) (event.getY() / size.height);
        return new Position(maybeReverse(row, !displayWhite), column);
    }

    private List<Position> getEndPositions(State state, Position clickPosition) {
        var endPositions = new ArrayList<Position>();
        for (var move : state.moves()) {
            if (move.start.equals(clickPosition)) {
                endPositions.add(move.end);
            }
        }
        return endPositions;
    }

    private List<Move> getPossibleMoves(State state, Position moveStart, Position clickPosition) {
        var possibleMoves = new ArrayList<Move>();
        for (var move : state.moves()) {
            if (move.start.equals(moveStart) && move.end.equals(clickPosition)) {
                possibleMoves.add(move);
            }
        }
        return possibleMoves;
    }

    private List<Piece.Type> getPiecePromotions(List<Move> possibleMoves) {
        var promotions = new ArrayList<Piece.Type>();
        for (var move : possibleMoves) {
            var promotion = move.promotionPieceType();
            promotion.ifPresent(promotions::add);
        }
        return promotions;
    }

    private Piece.Type getPromotionPieceFromUser(List<Piece.Type> promotions) {
        Piece.Type promoteTo = null;
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
        return promoteTo;
    }

    private Move getSelectedMove(List<Move> possibleMoves, Piece.Type promoteTo) {
        for (var move : possibleMoves) {
            var promotion = move.promotionPieceType();
            if (promotion.isPresent() && promotion.get() != promoteTo) {
                continue;
            }
            return move;
        }
        throw new IllegalStateException("No selected move");
    }

    private void alertUserTerminatedGame(State state, String winStatus) {
        String message;
        if (state.isCheckmate()) {
            message = winStatus + " the game due to " + state.terminalMessage();
        } else {
            message = "You have tied the game due to " + state.terminalMessage();
        }
        var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void paintBoard(Game game, SceneSize size) {
        paintBoardInternal(game, size, List.of(), !displayWhite);
    }

    private void paintBoard(Game game, SceneSize size, List<Position> endPositions) {
        paintBoardInternal(game, size, endPositions, !displayWhite);
    }

    private void paintBoardInternal(Game game, SceneSize size, List<Position> endPositions, boolean isReverse) {
        board.getChildren().clear();
        var squares = game.getBoard();
        for (int i = 0; i < ROW_COUNT; i++) {
            int index = maybeReverse(i, isReverse);
            for (int j = 0; j < COLUMN_COUNT; j++) {
                var r = new Rectangle(size.width, size.height);
                var background = getBackground(i, j, endPositions);
                r.setFill(background);
                var piece = squares[i][j];
                if (piece != null) {
                    var image = getPieceImage(piece, background);
                    r.setFill(image);
                }
                board.add(r, j, index);
            }
        }
    }

    private int maybeReverse(int index, boolean isReverse) {
        if (!isReverse) {
            return index;
        }
        return ROW_COUNT - 1 - index;
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
