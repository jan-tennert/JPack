module de.Jan.JPack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens de.Jan.JPack to javafx.fxml;
    exports de.Jan.JPack;
}