package frontend;

import backend.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.io.IOException;

public final class SettingsController {
    private Color playerColor = Color.WHITE;
    private Players playerCount = Players.ONE_PLAYER;

    @FXML
    private RadioButton white;

    @FXML
    private RadioButton black;

    @FXML
    private void onOnePlayer() {
        playerCount = Players.ONE_PLAYER;
        white.setVisible(true);
        black.setVisible(true);
    }

    @FXML
    private void onTwoPlayer() {
        playerCount = Players.TWO_PLAYERS;
        white.setVisible(false);
        black.setVisible(false);
    }

    @FXML
    private void onWhite() {
        playerColor = Color.WHITE;
    }

    @FXML
    private void onBlack() {
        playerColor = Color.BLACK;
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
