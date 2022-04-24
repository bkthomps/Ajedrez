package frontend;

import javafx.scene.Scene;

final class SceneSize {
    final double width;
    final double height;

    SceneSize(Scene scene) {
        width = scene.getWidth() / BoardController.COLUMN_COUNT;
        height = scene.getHeight() / BoardController.ROW_COUNT;
    }
}
