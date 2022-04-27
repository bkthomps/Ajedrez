module ajedrez {
    requires javafx.controls;
    requires javafx.fxml;

    opens frontend to javafx.fxml;
    exports frontend;
    exports backend;
}
