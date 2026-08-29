package theknife.ui.javafx;

import it.uninsubria.dto.RistoranteDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import theknife.model.*;
import theknife.utilities.Etichette;
import theknife.utilities.Finestre;
import theknife.utilities.Temi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;


/**
 * Controller della dashboard del ristoratore.
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 */
public class DashboardController {

    @FXML private Label etichettaUtente;
    @FXML private Button bottoneTema;

    @FXML private Label valoreLocali;
    @FXML private Label valoreMedia;
    @FXML private Label valoreRecensioni;
    @FXML private Label valoreSenzaRisposta;
    @FXML private VBox tileMedia;
    @FXML private VBox tileSenzaRisposta;

    @FXML private VBox contenitoreRistoranti;
    @FXML private VBox statoVuoto;

    /** Ristoranti di cui l'utente collegato è proprietario. */
    private LinkedList<RistoranteDTO> mieiRistoranti = new LinkedList<>();

    /**
     * Le classi che colorano il riquadro della media, tenute qui per poterle
     * togliere tutte prima di rimettere quella giusta: senza, a ogni
     * aggiornamento il riquadro accumulerebbe i colori dei voti precedenti.
     */
    private static final String[] CLASSI_MEDIA = {
            "stat-tile-rating-high",
            "stat-tile-rating-mid",
            "stat-tile-rating-low"
    };

    /**
     * Prepara la dashboard con i dati del ristoratore collegato.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() throws IOException {
        aggiornaPulsanteTema();

        Session sessione = Session.getInstance();
        etichettaUtente.setText("Ristoratore: " + (sessione.getUsername() == null ? "" : sessione.getUsername()));

        aggiornaTutto();
    }

    /**
     * Rilegge i propri ristoranti e ridisegna numeri ed elenco.
     * Va richiamato dopo ogni operazione che può averli cambiati.
     *
     * @author Matteo Franguelli
     */
    private void aggiornaTutto() throws IOException {
        caricaMieiRistoranti();
        aggiornaNumeri();
        costruisciElenco();
    }

    /**
     * Recupera dal db i ristoranti posseduti.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void caricaMieiRistoranti() throws IOException {
        mieiRistoranti = new LinkedList<>();

        Session sessione = Session.getInstance();

        String username = sessione.getUsername();
        if (username == null) return;

        mieiRistoranti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("RIST",sessione.getID());
    }

    /**
     * Calcola e mostra i quattro numeri in evidenza.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void aggiornaNumeri() {
        int totaleRecensioni = 0;
        int totaleSenzaRisposta = 0;
        double sommaVoti = 0;

        for (RistoranteDTO r : mieiRistoranti) {
            int quante = r.getNumeroRecensioni();
            double media = r.getMediaStelle();

            totaleRecensioni += quante;
            totaleSenzaRisposta += r.getNumeroRecensioniSenzaRisposta();

            if (quante > 0 && media > 0) {
                sommaVoti += media * quante;
            }
        }

        valoreLocali.setText(String.valueOf(mieiRistoranti.size()));
        valoreRecensioni.setText(String.valueOf(totaleRecensioni));
        valoreSenzaRisposta.setText(String.valueOf(totaleSenzaRisposta));

        if (totaleRecensioni > 0) {
            double media = sommaVoti / totaleRecensioni;
            valoreMedia.setText(String.format(Locale.ITALY, "★ %.1f", media));
            coloraRiquadroMedia(media);
        } else {
            valoreMedia.setText("—");
            coloraRiquadroMedia(-1);
        }

        // Quando non ci sono recensioni senza risposta il riquadro viene disattivato
        boolean cSonoRispostePendenti = totaleSenzaRisposta > 0;
        tileSenzaRisposta.setDisable(!cSonoRispostePendenti);

        tileSenzaRisposta.setAccessibleText(cSonoRispostePendenti
                ? totaleSenzaRisposta + " recensioni senza risposta, apri l'elenco per rispondere"
                : "Nessuna recensione senza risposta");
    }

    /**
     * Tinge il riquadro della media con il colore che spetta al voto:
     * verde dalle 4 stelle in su, giallo fra le 2 e le 4, rosso sotto le 2.
     * Con un voto negativo (nessuna recensione) il riquadro resta neutro,
     * perché non c'è nessun giudizio da rappresentare.
     *
     * @param media media delle recensioni, oppure un valore negativo se non
     *              ci sono ancora recensioni
     * @author Matteo Franguelli
     */
    private void coloraRiquadroMedia(double media) {
        tileMedia.getStyleClass().removeAll(CLASSI_MEDIA);

        if (media < 0) return;

        if (media >= 4)      tileMedia.getStyleClass().add("stat-tile-rating-high");
        else if (media >= 2) tileMedia.getStyleClass().add("stat-tile-rating-mid");
        else                 tileMedia.getStyleClass().add("stat-tile-rating-low");
    }

    /**
     * Ricostruisce l'elenco dei propri ristoranti, uno per riga.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void costruisciElenco() {
        contenitoreRistoranti.getChildren().clear();

        boolean vuoto = mieiRistoranti.isEmpty();
        statoVuoto.setVisible(vuoto);
        statoVuoto.setManaged(vuoto);
        contenitoreRistoranti.setVisible(!vuoto);
        contenitoreRistoranti.setManaged(!vuoto);

        for (RistoranteDTO r : mieiRistoranti) {
            contenitoreRistoranti.getChildren().add(creaRiga(r));
        }
    }

    /**
     * Crea la riga di un ristorante gestito: nome e città a sinistra, media e
     * riconoscimenti a destra. Cliccandola si aprono le sue recensioni.
     *
     * @param r ristorante da rappresentare
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private HBox creaRiga(RistoranteDTO r) {
        Label nome = new Label(r.getNome() == null ? "" : r.getNome());
        nome.getStyleClass().add("restaurant-name");

        String citta = r.getLuogo() == null ? "" : r.getLuogo().getCitta().getNome();
        Label luogo = new Label(citta == null ? "" : citta);
        luogo.getStyleClass().add("card-line-text");

        VBox testi = new VBox(nome, luogo);
        testi.getStyleClass().add("owner-card-text");

        Region spaziatore = new Region();
        HBox.setHgrow(spaziatore, Priority.ALWAYS);

        HBox badge = new HBox();
        badge.getStyleClass().add("owner-card-tags");
        badge.getChildren().add(Etichette.creaBadgeMedia(r));

        Label michelin = Etichette.creaBadgeMichelin(r);
        if (michelin != null) badge.getChildren().add(michelin);

        int senzaRisposta = r.getNumeroRecensioniSenzaRisposta();
        if (senzaRisposta > 0) {
            badge.getChildren().add(Etichette.creaBadge(senzaRisposta + " da rispondere", "tag-todo"));
        }

        HBox riga = new HBox(testi, spaziatore, badge);
        riga.getStyleClass().add("owner-card");
        riga.setOnMouseClicked(evento -> {
            try {
                apriRecensioniDi(r);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        riga.setAccessibleText("Ristorante " + r.getNome() + ", apri le recensioni");

        return riga;
    }

    /**
     * Apre le recensioni di un singolo ristorante gestito.
     * @author Matteo Franguelli
     */
    private void apriRecensioniDi(RistoranteDTO r) throws IOException {
        aggiornaTutto();
        Finestre.apriModale("view_reviews.fxml", "Recensioni di " + r.getNome(),
                (ViewReviewsController ctrl) -> ctrl.setRestaurant(r));
    }

    /**
     * Apre la finestra per rispondere alle recensioni ancora senza risposta.
     * @author Matteo Franguelli
     */
    @FXML
    private void onRecensioniSenzaRisposta() throws IOException {
        Finestre.apriModale("reply_review.fxml", "Recensioni a cui rispondere");
        aggiornaTutto();
    }

    /**
     * Apre la finestra per aggiungere un nuovo ristorante.
     * @author Matteo Franguelli
     */
    @FXML
    private void onAggiungiRistorante() throws IOException {
        Finestre.apriModale("add_restaurant.fxml", "Nuovo ristorante");
        aggiornaTutto();
    }

    /**
     * Disconnette l'utente e torna alla schermata di benvenuto.
     * @author Matteo Franguelli
     */
    @FXML
    private void onLogout() {
        Session.getInstance().logout();
        Finestre.cambiaVista(etichettaUtente.getScene(), "welcome.fxml");
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
        bottoneTema.setTooltip(new Tooltip(Temi.descrizionePulsante()));
        bottoneTema.setAccessibleText(Temi.descrizionePulsante());
    }
}
