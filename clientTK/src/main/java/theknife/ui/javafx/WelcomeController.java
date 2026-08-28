package theknife.ui.javafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import theknife.model.GestioneRistoranti;
import theknife.utilities.Finestre;
import theknife.utilities.Temi;
import theknife.utilities.Utility;

import java.io.IOException;
import java.util.Map;

/**
 * Controller della schermata di benvenuto.
 * @author Matteo Franguelli
 */
public class WelcomeController implements ControllerAutenticazione {

    @FXML private ComboBox<String> campoCitta;
    @FXML private Button bottoneOspite;
    @FXML private Button bottoneTema;
    @FXML private Label etichettaStato;
    @FXML private Label etichettaErrore;

    /** Città disponibili, con quanti ristoranti contengono. */
    private Map<String, Integer> cittaDisponibili;

    private final GestioneRistoranti gestioneRistoranti = GestioneRistoranti.getInstance();

    /**
     * Prepara la schermata e avvia la lettura del catalogo.
     * Finché il catalogo non è pronto l'ingresso come ospite resta disabilitato:
     * senza l'elenco delle città non ci sarebbe nulla da scegliere.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        aggiornaPulsanteTema();
        Utility.completamentoCitta(campoCitta);
        Utility.confermaConInvio(bottoneOspite, campoCitta);

        etichettaErrore.setText("");
        bottoneOspite.setDisable(true);
        etichettaStato.setText("Caricamento del catalogo dei ristoranti…");

        caricaTendina();
    }

    /**
     * Legge il catalogo in un thread separato e, al termine, riempie la
     * tendina delle città sul thread grafico.
     *
     * @author Matteo Franguelli
     */
    private void caricaTendina() {
        new Thread(() -> {
            //TODO da sostituire il csv con il comando che creeremo apposta per ottenere la lista dei nomi delle città
            gestioneRistoranti.caricaDaCsv();
            Map<String, Integer> citta = gestioneRistoranti.getCittaConConteggio();

            Platform.runLater(() -> {
                cittaDisponibili = citta;

                bottoneOspite.setDisable(false);
                etichettaStato.setText(citta.size() + " città disponibili");
            });
        }).start();
    }

    /**
     * Apre la finestra di accesso.
     * @author Matteo Franguelli
     */
    @FXML
    private void onAccedi() {
        Finestre.apriModale("login.fxml", "Accedi",
                (LoginController ctrl) -> ctrl.setParentController(this));
    }

    /**
     * Apre la finestra di registrazione.
     * @author Matteo Franguelli
     */
    @FXML
    private void onRegistrati() {
        Finestre.apriModale("register.fxml", "Registrati",
                (RegisterController ctrl) -> ctrl.setParentController(this));
    }

    /**
     * Chiamato da accesso e registrazione quando vanno a buon fine:
     * porta l'utente alla schermata giusta per il suo ruolo.
     *
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @Override
    public void onLoginSuccess() throws IOException {
        Session sessione = Session.getInstance();
        if (!sessione.isAuthenticated()) return;

        sessione.aggiornaDatiUtente();

        if (sessione.isRistoratore()) {
            apriDashboardRistoratore();
        } else {
            apriSchermataPrincipale(sessione.getCitta());
        }
    }

    /**
     * Entra come ospite dopo aver verificato il luogo indicato.
     * Il luogo è obbligatorio e deve esistere nel catalogo: così l'ospite non
     * si ritrova davanti a una lista vuota per un errore di battitura.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onOspite() throws IOException {
        String scritta = campoCitta.getEditor().getText();
        if (scritta == null || scritta.isBlank()) {
            scritta = campoCitta.getValue();
        }

        if (scritta == null || scritta.isBlank()) {
            mostraErrore("Indica da dove stai cercando: il luogo è obbligatorio.");
            campoCitta.requestFocus();
            return;
        }

        String citta = trovaCitta(scritta.trim());
        if (citta == null) {
            mostraErrore("Non ci sono ristoranti a \"" + scritta.trim() + "\". Scegli una città dall'elenco.");
            campoCitta.requestFocus();
            return;
        }

        etichettaErrore.setText("");

        Session sessione = Session.getInstance();
        sessione.login(null, Session.Role.GUEST);
        sessione.setCitta(citta);

        apriSchermataPrincipale(citta);
    }

    /**
     * Cerca una città nel catalogo ignorando maiuscole e minuscole e
     * restituisce il nome esatto con cui è scritta nel file.
     *
     * @return il nome della città, oppure null se non esiste nel catalogo
     * @author Matteo Franguelli
     */
    private String trovaCitta(String scritta) {
        if (cittaDisponibili == null) return null;

        for (String citta : cittaDisponibili.keySet()) {
            if (citta.equalsIgnoreCase(scritta)) return citta;
        }
        return null;
    }

    /**
     * Cambia il tema di tutta l'applicazione.
     * @author Matteo Franguelli
     */
    @FXML
    private void onCambiaTema() {
        Temi.alterna();
        aggiornaPulsanteTema();
    }

    /**
     * Allinea simbolo e descrizione del pulsante al tema in uso.
     * @author Matteo Franguelli
     */
    private void aggiornaPulsanteTema() {
        bottoneTema.setText(Temi.simboloPulsante());
        bottoneTema.setTooltip(new javafx.scene.control.Tooltip(Temi.descrizionePulsante()));
        bottoneTema.setAccessibleText(Temi.descrizionePulsante());
    }

    /**
     * Sostituisce il contenuto della finestra con la schermata principale,
     * impostando la città di partenza della ricerca.
     *
     * @param cittaIniziale città con cui pre-compilare la ricerca, può essere null
     * @author Matteo Franguelli
     */
    private void apriSchermataPrincipale(String cittaIniziale) {
        MainController controller = (MainController) sostituisciContenuto("main.fxml");
        if (controller != null && cittaIniziale != null && !cittaIniziale.isBlank()) {
            controller.impostaLuogoIniziale(cittaIniziale);
        }
    }

    /**
     * Sostituisce il contenuto della finestra con la dashboard del ristoratore.
     * @author Matteo Franguelli
     */
    private void apriDashboardRistoratore() {
        sostituisciContenuto("dashboard.fxml");
    }

    /**
     * Carica una vista e la mette al posto della schermata di benvenuto,
     * riutilizzando la stessa finestra invece di aprirne una nuova.
     *
     * @param nomeFxml nome del file FXML da caricare
     * @return il controller della vista caricata, oppure null in caso di errore
     * @author Matteo Franguelli
     */
    private Object sostituisciContenuto(String nomeFxml) {
        return Finestre.cambiaVista(campoCitta.getScene(), nomeFxml);
    }

    /**
     * Mostra un messaggio di errore sotto il campo del luogo.
     * @author Matteo Franguelli
     */
    private void mostraErrore(String messaggio) {
        etichettaErrore.setText(messaggio);
    }
}
