/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
module it.uninsubria.theknifeui {
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
