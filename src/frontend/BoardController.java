package frontend;

import backend.*;
import bot.BotTurn;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.concurrent.Semaphore;

public final class BoardController {
    private static final ResourceBundle RESOURCE = ResourceBundle.getBundle("i18n/ajedrez", Locale.getDefault());

    static final int ROW_COUNT = 8;
    static final int COLUMN_COUNT = 8;
    private static final Color DARK_SQUARE = Color.rgb(160, 80, 0);
    private static final Color LIGHT_SQUARE = Color.rgb(200, 100, 0);
    private static final Color DARK_HIGHLIGHTED = Color.rgb(180, 160, 140);
    private static final Color LIGHT_HIGHLIGHTED = Color.rgb(200, 160, 140);
    private static final Color KING_CHECKED = Color.rgb(250, 90, 80);
    private static final AudioClip MOVE_SOUND
            = new AudioClip(Objects.requireNonNull(BoardController.class.getResource("/move.wav")).toExternalForm());
    private static final AudioClip ERROR_SOUND
            = new AudioClip(Objects.requireNonNull(BoardController.class.getResource("/error.wav")).toExternalForm());

    private final Semaphore semaphore = new Semaphore(1);
    private Game game;
    private State state;
    private Position moveStart;
    private boolean displayWhite;
    private Players players;
    private Piece[][] boardPieces;

    @FXML
    private GridPane board;

    void setPlayerData(PlayerData player, Scene scene) throws InterruptedException {
        var size = new SceneSize(scene);
        game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        displayWhite = (player.color == backend.Color.WHITE);
        players = player.count;
        paintBoardAfterMove(game, size);
        scene.widthProperty().addListener((observed, oldWidth, width) -> paintLastBoard(new SceneSize(scene)));
        scene.heightProperty().addListener((observed, oldHeight, height) -> paintLastBoard(new SceneSize(scene)));
        if (player.color != backend.Color.BLACK || player.count != Players.ONE_PLAYER) {
            state = game.generateMoves();
            paintBoard(game, size);
            if (state.isTerminal()) {
                alertUserTerminatedGame(state, getTerminalMessage());
            }
            return;
        }
        semaphore.acquire();
        botMove(size);
    }

    private String getTerminalMessage() {
        if (players == Players.ONE_PLAYER) {
            return RESOURCE.getString("userLost");
        }
        if (displayWhite) {
            return RESOURCE.getString("blackWon");
        }
        return RESOURCE.getString("whiteWon");
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        boolean hadPermit = semaphore.tryAcquire();
        if (!hadPermit) {
            ERROR_SOUND.play();
            return;
        }
        var scene = ((Node) event.getSource()).getScene();
        var size = new SceneSize(scene);
        var clickPosition = getClickPosition(size, event);
        boolean isDone = userMove(size, clickPosition);
        if (isDone) {
            semaphore.release();
            return;
        }
        userMove(size, clickPosition);
        botMove(size);
    }

    private boolean userMove(SceneSize size, Position clickPosition) {
        if (state.isTerminal()) {
            return true;
        }
        if (moveStart == null) {
            var endPositions = getEndPositions(state, clickPosition);
            if (endPositions.isEmpty()) {
                moveStart = null;
                return true;
            }
            moveStart = clickPosition;
            paintBoard(game, size, endPositions);
            return true;
        }
        var possibleMoves = getPossibleMoves(state, moveStart, clickPosition);
        moveStart = null;
        if (possibleMoves.isEmpty()) {
            paintBoard(game, size);
            return true;
        }
        var promotions = getPiecePromotions(possibleMoves);
        Piece.Type promoteTo = null;
        if (!promotions.isEmpty()) {
            promoteTo = getPromotionPieceFromUser(promotions);
        }
        var move = getSelectedMove(possibleMoves, promoteTo);
        move.perform();
        MOVE_SOUND.play();
        if (players == Players.TWO_PLAYERS) {
            displayWhite = !displayWhite;
            paintBoard(game, size);
            state = game.generateMoves();
            paintBoard(game, size);
            if (state.isTerminal()) {
                alertUserTerminatedGame(state, getTerminalMessage());
            }
            return true;
        }
        paintBoardAfterMove(game, size);
        return false;
    }

    private void botMove(SceneSize size) {
        var botMove = new Task<>() {
            @Override
            protected Void call() {
                state = BotTurn.perform(game);
                return null;
            }
        };
        botMove.setOnSucceeded(e -> {
            try {
                MOVE_SOUND.play();
                paintBoardAfterMove(game, size);
                if (state.isTerminal()) {
                    alertUserTerminatedGame(state, RESOURCE.getString("userWon"));
                    return;
                }
                state = game.generateMoves();
                paintBoard(game, size);
                if (state.isTerminal()) {
                    alertUserTerminatedGame(state, getTerminalMessage());
                }
            } finally {
                semaphore.release();
            }
        });
        new Thread(botMove).start();
    }

    private Position getClickPosition(SceneSize size, MouseEvent event) {
        int column = (int) (event.getX() / size.width);
        column = maybeReverse(COLUMN_COUNT, column, !displayWhite);
        int row = (int) (event.getY() / size.height);
        row = maybeReverse(ROW_COUNT, row, !displayWhite);
        return new Position(row, column);
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
            buttons[i] = new ButtonType(RESOURCE.getString(promotions.get(i).toString().toLowerCase()));
        }
        var alert = new Alert(Alert.AlertType.NONE, RESOURCE.getString("pawnPromotion"), buttons);
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
            message = winStatus;
        } else {
            message = RESOURCE.getString("userTied");
        }
        message += " " + RESOURCE.getString("theGame") + " " + RESOURCE.getString(state.terminalType());
        var alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void paintBoard(Game game, SceneSize size) {
        paintBoard(game, size, List.of());
    }

    private void paintBoardAfterMove(Game game, SceneSize size) {
        boardPieces = game.getBoard();
        paintBoardInternal(boardPieces, size, List.of(), !displayWhite, false);
    }

    private void paintBoard(Game game, SceneSize size, List<Position> endPositions) {
        boardPieces = game.getBoard();
        paintBoardInternal(boardPieces, size, endPositions, !displayWhite, true);
    }

    private void paintLastBoard(SceneSize size) {
        paintBoardInternal(boardPieces, size, List.of(), !displayWhite, true);
    }

    private void paintBoardInternal(Piece[][] squares, SceneSize size,
                                    List<Position> endPositions, boolean isReverse, boolean withCheck) {
        board.getChildren().clear();
        for (int i = 0; i < ROW_COUNT; i++) {
            int row = maybeReverse(ROW_COUNT, i, isReverse);
            for (int j = 0; j < COLUMN_COUNT; j++) {
                int column = maybeReverse(COLUMN_COUNT, j, isReverse);
                var r = new Rectangle(size.width, size.height);
                var background = getBackground(i, j, endPositions);
                r.setFill(background);
                var piece = squares[i][j];
                if (piece != null) {
                    boolean isPieceWhite = (piece.color == backend.Color.WHITE);
                    boolean isPlayerKing = (piece.type == Piece.Type.KING) && (isPieceWhite == displayWhite);
                    boolean isKingChecked = withCheck && (state.isTerminal() ? state.isCheckmate() : state.isCheck());
                    boolean inCheck = isPlayerKing && isKingChecked;
                    var image = getPieceImage(piece, background, inCheck);
                    r.setFill(image);
                }
                board.add(r, column, row);
            }
        }
    }

    private int maybeReverse(int count, int index, boolean isReverse) {
        if (!isReverse) {
            return index;
        }
        return count - 1 - index;
    }

    private Color getBackground(int i, int j, List<Position> endPositions) {
        boolean isLightSquare = (i + j) % 2 == 0;
        var background = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        for (var end : endPositions) {
            if (end.row == i && end.column == j) {
                background = isLightSquare ? LIGHT_HIGHLIGHTED : DARK_HIGHLIGHTED;
                break;
            }
        }
        return background;
    }

    private ImagePattern getPieceImage(Piece piece, Color background, boolean inCheck) {
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
                var pixel = background;
                if (oldPixel.isOpaque()) {
                    pixel = oldPixel;
                    if (inCheck) {
                        double blackCutoff = 0.4;
                        double r = oldPixel.getRed();
                        double g = oldPixel.getGreen();
                        double b = oldPixel.getBlue();
                        if (r > blackCutoff && g > blackCutoff && b > blackCutoff) {
                            pixel = KING_CHECKED;
                        }
                    }
                }
                writer.setColor(x, y, pixel);
            }
        }
        return new ImagePattern(outputImage);
    }
}
