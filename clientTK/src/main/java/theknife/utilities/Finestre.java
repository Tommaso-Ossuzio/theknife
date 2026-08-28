package theknife.utilities;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Apertura delle finestre secondarie dell'applicazione.
 * @author Matteo Franguelli
 */
public final class Finestre {

    /** Cartella delle viste dentro le risorse. */
    private static final String CARTELLA_VISTE = "/it/unininsubria/theknifeui/ui/javafx/view/";

    /** Schermo per cui sono state disegnate le misure delle viste. */
    private static final double LARGHEZZA_PROGETTO = 1920;
    private static final double ALTEZZA_PROGETTO = 1080;

    /** Classe di sole utilità: non va istanziata. */
    private Finestre() {
    }

    /**
     * Apre una finestra modale e attende che venga chiusa.
     *
     * @param nomeFxml            nome del file FXML, senza percorso (es. "login.fxml")
     * @param titolo              titolo della finestra
     * @param configuraController azione eseguita sul controller appena caricato,
     *                            prima che la finestra venga mostrata; può essere null
     *                            se il controller non va configurato
     * @param <T>                 tipo del controller della vista
     * @author Matteo Franguelli
     */
    public static <T> void apriModale(String nomeFxml, String titolo, Consumer<T> configuraController) {
        URL urlFxml = Finestre.class.getResource(CARTELLA_VISTE + nomeFxml);
        if (urlFxml == null) {
            System.err.println("Vista non trovata: " + nomeFxml);
            return;
        }

        try {
            FXMLLoader caricatore = new FXMLLoader(urlFxml);
            Parent radice = caricatore.load();
            Scene scena = new Scene(radice);

            Temi.registra(scena);

            Stage finestra = new Stage();
            finestra.setScene(scena);
            finestra.setTitle(titolo);
            finestra.initModality(Modality.APPLICATION_MODAL);

            if (radice instanceof Region area) {
                adattaAlloSchermo(finestra, area);
            }

            if (configuraController != null) {
                T controller = caricatore.getController();
                configuraController.accept(controller);
            }

            finestra.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ingrandisce la finestra in proporzione allo schermo, partendo dalle misure
     * con cui la vista è stata disegnata: sotto quelle misure non si scende mai,
     * perché il contenuto verrebbe tagliato. Le misure sono quelle del contenuto,
     * alla cornice della finestra pensa {@link Stage#sizeToScene()}.
     *
     * @param finestra finestra da ridimensionare
     * @param radice   radice della vista, con le misure di progetto
     * @author Matteo Franguelli
     */
    public static void adattaAlloSchermo(Stage finestra, Region radice) {
        if (finestra == null || radice == null) return;
        if (radice.getPrefWidth() <= 0 || radice.getPrefHeight() <= 0) return;

        Rectangle2D schermo = Screen.getPrimary().getVisualBounds();
        double fattore = Math.max(1, Math.min(schermo.getWidth() / LARGHEZZA_PROGETTO,
                schermo.getHeight() / ALTEZZA_PROGETTO));

        radice.setPrefWidth(radice.getPrefWidth() * fattore);
        radice.setPrefHeight(radice.getPrefHeight() * fattore);
        finestra.sizeToScene();

        if (finestra.getWidth() > schermo.getWidth()) finestra.setWidth(schermo.getWidth());
        if (finestra.getHeight() > schermo.getHeight()) finestra.setHeight(schermo.getHeight());

        finestra.centerOnScreen();
    }

    /**
     * Apre una finestra modale che non ha bisogno di configurare il controller.
     *
     * @param nomeFxml nome del file FXML, senza percorso
     * @param titolo   titolo della finestra
     * @author Matteo Franguelli
     */
    public static void apriModale(String nomeFxml, String titolo) {
        apriModale(nomeFxml, titolo, null);
    }

    /**
     * Sostituisce il contenuto di una finestra già aperta con un'altra vista.
     * @param scena    finestra da riutilizzare
     * @param nomeFxml nome del file FXML da caricare
     * @return il controller della vista caricata, oppure null se il caricamento fallisce
     * @author Matteo Franguelli
     */
    public static Object cambiaVista(Scene scena, String nomeFxml) {
        if (scena == null) return null;

        URL urlFxml = Finestre.class.getResource(CARTELLA_VISTE + nomeFxml);
        if (urlFxml == null) {
            System.err.println("Vista non trovata: " + nomeFxml);
            return null;
        }

        try {
            FXMLLoader caricatore = new FXMLLoader(urlFxml);
            scena.setRoot(caricatore.load());

            Temi.applicaA(scena);

            return caricatore.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
