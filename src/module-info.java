module ajedrez {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens frontend to javafx.fxml;
    exports frontend;
    exports backend;
}
