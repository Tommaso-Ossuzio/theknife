package theknife;

import javafx.application.Application;
import theknife.ui.javafx.MainApp;

/**
 * Classe di lancio dell'applicazione.
 *
 * Questa classe esiste solo per avviare JavaFX.
 * Non inserire logica di interfaccia o controller qui dentro.
 */
public class TheKnife {

    /**
     * Metodo principale del programma.
     * Avvia l'applicazione JavaFX caricando la classe MainApp.
     */
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);


    }
}