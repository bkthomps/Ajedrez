package frontend;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public final class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        var loader = new FXMLLoader(getClass().getResource("/fxml/settings-view.fxml"));
        var scene = new Scene(loader.load());
        stage.setTitle("Ajedrez");
        stage.getIcons().add(new Image("BLACK_KING.png"));
        stage.setScene(scene);
        stage.show();
        var controller = (SettingsController) loader.getController();
        controller.sizeScene(scene);
        scene.widthProperty().addListener((observed, oldWidth, width) -> controller.sizeScene(scene));
        scene.heightProperty().addListener((observed, oldHeight, height) -> controller.sizeScene(scene));
    }

    public static void main(String[] args) {
        launch();
    }
}
