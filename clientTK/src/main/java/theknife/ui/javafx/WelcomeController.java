package theknife.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import theknife.model.GestioneRistoranti;

import java.util.Map;

/**
 * Controller della schermata di benvenuto.
 * <p>
 * È la prima schermata dell'applicazione e ha tre compiti: proporre le tre
 * strade possibili (accedere, registrarsi, entrare come ospite), leggere il
 * catalogo dei ristoranti in secondo piano e, per chi entra come ospite,
 * raccogliere il luogo da cui sta cercando.
 * <p>
 * Il catalogo viene letto qui e non più nella schermata principale perché
 * l'elenco delle città da proporre all'ospite si ricava proprio da quel file:
 * va quindi conosciuto prima che la schermata principale venga aperta.
 *
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

    /** Elenco completo delle città, da cui la tendina filtra mentre si scrive. */
    private final ObservableList<String> tutteLeCitta = FXCollections.observableArrayList();

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
        preparaTendinaCitta();

        etichettaErrore.setText("");
        bottoneOspite.setDisable(true);
        etichettaStato.setText("Caricamento del catalogo dei ristoranti…");

        caricaCatalogo();
    }

    /**
     * Legge il catalogo in un thread separato e, al termine, riempie la
     * tendina delle città sul thread grafico.
     *
     * @author Matteo Franguelli
     */
    private void caricaCatalogo() {
        new Thread(() -> {
            gestioneRistoranti.caricaDaCsv();
            Map<String, Integer> citta = gestioneRistoranti.getCittaConConteggio();

            Platform.runLater(() -> {
                cittaDisponibili = citta;
                tutteLeCitta.setAll(citta.keySet());

                bottoneOspite.setDisable(false);
                etichettaStato.setText(citta.size() + " città disponibili");
            });
        }).start();
    }

    /**
     * Configura la tendina delle città: si può scrivere dentro e l'elenco si
     * restringe alle città che iniziano con quanto digitato. Ogni voce mostra
     * anche quanti ristoranti contiene, così la scelta è informata.
     *
     * @author Matteo Franguelli
     */
    private void preparaTendinaCitta() {
        FilteredList<String> filtrate = new FilteredList<>(tutteLeCitta, c -> true);
        campoCitta.setItems(filtrate);

        campoCitta.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(String citta, boolean vuota) {
                super.updateItem(citta, vuota);
                if (vuota || citta == null) {
                    setText(null);
                } else {
                    int quanti = cittaDisponibili == null ? 0 : cittaDisponibili.getOrDefault(citta, 0);
                    setText(citta + "   ·   " + quanti + (quanti == 1 ? " ristorante" : " ristoranti"));
                }
            }
        });

        campoCitta.getEditor().textProperty().addListener((osservato, precedente, digitato) -> {
            // Se il testo coincide con la voce già scelta, l'utente non sta
            // digitando: sta solo vedendo il risultato della selezione.
            String selezionata = campoCitta.getSelectionModel().getSelectedItem();
            if (selezionata != null && selezionata.equals(digitato)) return;

            String ricerca = digitato == null ? "" : digitato.trim().toLowerCase();
            filtrate.setPredicate(citta -> ricerca.isEmpty() || citta.toLowerCase().startsWith(ricerca));

            if (!ricerca.isEmpty() && !filtrate.isEmpty()) {
                campoCitta.show();
            } else {
                campoCitta.hide();
            }
        });
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
     */
    @Override
    public void onLoginSuccess() {
        Session sessione = Session.getInstance();
        if (!sessione.isAuthenticated()) return;

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
    private void onOspite() {
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
        // Nome letto dagli strumenti di accessibilità, che sul solo simbolo
        // non saprebbero dire a cosa serve il pulsante
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
