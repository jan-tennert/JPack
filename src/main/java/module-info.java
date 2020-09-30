module de.Jan.JPack {
    requires javafx.controls;
    requires javafx.fxml;

    opens de.Jan.JPack to javafx.fxml;
    exports de.Jan.JPack;
}