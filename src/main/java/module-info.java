module de.Jan.JPack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;
    requires static lombok;

    opens de.Jan.JPack to javafx.fxml, com.google.gson;
    exports de.Jan.JPack;
}