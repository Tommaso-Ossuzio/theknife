package theknife.ui.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Apertura delle finestre secondarie dell'applicazione.
 * <p>
 * Ogni schermata secondaria di TheKnife è una finestra modale costruita
 * sempre allo stesso modo: si carica un FXML, se ne fa una scena, si crea
 * uno {@link Stage} modale, si configura il controller e si attende la
 * chiusura. Raccogliere qui quel blocco evita di ripeterlo per ogni
 * schermata e garantisce che nessuna finestra dimentichi di registrarsi
 * presso {@link Temi}: se lo dimenticasse, resterebbe chiara anche con il
 * tema scuro attivo.
 *
 * @author Matteo Franguelli
 */
public final class Finestre {

    /** Cartella delle viste dentro le risorse. */
    private static final String CARTELLA_VISTE = "/it/unininsubria/theknifeui/ui/javafx/view/";

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
            Scene scena = new Scene(caricatore.load());

            // Senza questa riga la finestra ignorerebbe il tema scuro
            Temi.registra(scena);

            Stage finestra = new Stage();
            finestra.setScene(scena);
            finestra.setTitle(titolo);
            finestra.initModality(Modality.APPLICATION_MODAL);

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
     * <p>
     * L'applicazione non ha un sistema di navigazione: le schermate principali
     * (benvenuto, elenco dei ristoranti, dashboard del ristoratore) si
     * susseguono dentro la stessa finestra riassegnandone la radice, invece di
     * aprire finestre nuove che lascerebbero dietro di sé quelle vecchie.
     *
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

            // La radice appena caricata non ha ancora il foglio del tema scuro
            Temi.applicaA(scena);

            return caricatore.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
