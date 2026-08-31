module clientTK {
    requires commonTK;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.web;

    exports theknife;
    exports theknife.ui.javafx;
    opens theknife.ui.javafx to javafx.fxml;
    exports theknife.utilities;
    opens theknife.utilities to javafx.fxml;
}
