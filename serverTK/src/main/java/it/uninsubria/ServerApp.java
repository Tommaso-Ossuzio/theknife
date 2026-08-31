/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Finestra di avvio del server.
 * @author Celestino Resteghini
 */
public class ServerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent radice = FXMLLoader.load(getClass().getResource("/it/uninsubria/view/server.fxml"));
        stage.setTitle("Server TheKnife");
        stage.setScene(new Scene(radice));
        stage.setMinWidth(560);
        stage.setMinHeight(480);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }
}
