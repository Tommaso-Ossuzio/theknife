/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import theknife.utilities.Finestre;
import theknife.utilities.Temi;
import theknife.utilities.Utility;

import java.io.IOException;
import java.util.List;

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

    /** Città disponibili nel database. */
    private List<String> cittaDisponibili;

    /**
     * Prepara la schermata e chiede l'elenco delle città, senza il quale
     * l'ingresso come ospite resta disabilitato.
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
     * Carica le città in un thread separato e poi abilita l'ingresso come ospite.
     * @author Matteo Franguelli
     */
    private void caricaTendina() {
        new Thread(() -> {
            List<String> citta = Utility.elencoCitta();

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
     * Porta l'utente alla schermata adatta al suo ruolo dopo accesso o registrazione.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     * @author Michele Viselli
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
     * Entra come ospite, ma solo se la città indicata esiste nel catalogo.
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
     * Cerca una città ignorando maiuscole e minuscole.
     * @param scritta nome digitato dall'utente
     * @return il nome esatto della città, null se non esiste nel catalogo
     * @author Matteo Franguelli
     */
    private String trovaCitta(String scritta) {
        if (cittaDisponibili == null) return null;

        for (String citta : cittaDisponibili) {
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
     * Mostra l'elenco dei ristoranti nella stessa finestra.
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
     * Sostituisce la schermata di benvenuto con un'altra vista, senza aprire finestre.
     * @param nomeFxml nome del file FXML da caricare
     * @return il controller della vista caricata, oppure null in caso di errore
     * @author Matteo Franguelli
     */
    private Object sostituisciContenuto(String nomeFxml) {
        return Finestre.cambiaVista(campoCitta.getScene(), nomeFxml);
    }

    /**
     * Mostra un messaggio di errore sotto il campo della città.
     * @param messaggio testo da mostrare
     * @author Matteo Franguelli
     */
    private void mostraErrore(String messaggio) {
        etichettaErrore.setText(messaggio);
    }
}
