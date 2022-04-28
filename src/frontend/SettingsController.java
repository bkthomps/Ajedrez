package frontend;

import backend.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public final class SettingsController {
    private static final String IDLE_BUTTON_STYLE
            = "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
    private static final String HOVERED_BUTTON_STYLE
            = IDLE_BUTTON_STYLE + "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.7), 20,0,0,0 );";

    private Color playerColor = Color.WHITE;
    private Players playerCount = Players.ONE_PLAYER;

    @FXML
    private Button onePlayerButton;

    @FXML
    private Button twoPlayerButton;

    @FXML
    private Button whiteButton;

    @FXML
    private Button blackButton;

    @FXML
    private ImageView onePlayerIcon;

    @FXML
    private ImageView twoPlayerIcon;

    @FXML
    private ImageView whiteIcon;

    @FXML
    private ImageView blackIcon;

    @FXML
    private ImageView nextIcon;

    void sizeScene(Scene scene) {
        double height = scene.getHeight();
        double width = scene.getWidth();
        double widthPadding = 17;
        double heightPadding = 26;
        double halfWidth = width / 2 - widthPadding;
        double fullWidth = width - widthPadding;
        double playerCountHeight = 0.35 * height - heightPadding;
        double playerColorHeight = 0.4 * height - heightPadding;
        double nextButtonHeight = 0.25 * height - heightPadding;
        onePlayerIcon.setFitHeight(playerCountHeight);
        onePlayerIcon.setFitWidth(halfWidth);
        twoPlayerIcon.setFitHeight(playerCountHeight);
        twoPlayerIcon.setFitWidth(halfWidth);
        whiteIcon.setFitHeight(playerColorHeight);
        whiteIcon.setFitWidth(halfWidth);
        blackIcon.setFitHeight(playerColorHeight);
        blackIcon.setFitWidth(halfWidth);
        nextIcon.setFitHeight(nextButtonHeight);
        nextIcon.setFitWidth(fullWidth);
        refreshButtons();
    }

    private void refreshButtons() {
        if (playerCount == Players.TWO_PLAYERS) {
            whiteButton.setStyle(IDLE_BUTTON_STYLE);
            blackButton.setStyle(IDLE_BUTTON_STYLE);
            onePlayerButton.setStyle(IDLE_BUTTON_STYLE);
            twoPlayerButton.setStyle(HOVERED_BUTTON_STYLE);
            whiteButton.setDisable(true);
            blackButton.setDisable(true);
            return;
        }
        if (playerCount == Players.ONE_PLAYER) {
            onePlayerButton.setStyle(HOVERED_BUTTON_STYLE);
            twoPlayerButton.setStyle(IDLE_BUTTON_STYLE);
            whiteButton.setDisable(false);
            blackButton.setDisable(false);
        }
        if (playerColor == Color.WHITE) {
            whiteButton.setStyle(HOVERED_BUTTON_STYLE);
            blackButton.setStyle(IDLE_BUTTON_STYLE);
        } else {
            whiteButton.setStyle(IDLE_BUTTON_STYLE);
            blackButton.setStyle(HOVERED_BUTTON_STYLE);
        }
    }

    @FXML
    private void onOnePlayer() {
        playerCount = Players.ONE_PLAYER;
        refreshButtons();
    }

    @FXML
    private void onTwoPlayer() {
        playerCount = Players.TWO_PLAYERS;
        refreshButtons();
    }

    @FXML
    private void onWhite() {
        playerColor = Color.WHITE;
        refreshButtons();
    }

    @FXML
    private void onBlack() {
        playerColor = Color.BLACK;
        refreshButtons();
    }

    @FXML
    private void onStartGame(ActionEvent event) throws IOException, InterruptedException {
        var loader = new FXMLLoader(getClass().getResource("/fxml/board-view.fxml"));
        var scene = new Scene(loader.load());
        var window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        var controller = (BoardController) loader.getController();
        var playerData = new PlayerData(playerCount, playerColor);
        controller.setPlayerData(playerData, scene);
        window.show();
    }
}
