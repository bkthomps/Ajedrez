package frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {
    private String playerColor;
    private String playerCount;
    private String botDifficulty;

    @FXML
    protected void onStartGame(ActionEvent event) throws IOException {
        var loader = new FXMLLoader(getClass().getResource("board-view.fxml"));
        var scene = new Scene(loader.load(), 320, 240);
        var window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
}
