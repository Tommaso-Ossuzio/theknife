package theknife.ui.javafx;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RistoranteDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.model.*;
import theknife.utilities.Finestre;

import java.io.IOException;
import java.util.LinkedList;

//TODO da rivedere

/**
 * Controller che gestisce la visualizzazione e la gestione
 * delle recensioni scritte dall'utente corrente.
 *
 * @author Matteo Franguelli
 * @author Michele Viselli
 * @author Celestino Resteghini
 */
public class MyReviewsController {

    @FXML private TableView<ReviewRow> tabellaRecensioni;
    @FXML private TableColumn<ReviewRow, String> colonnaRistorante;
    @FXML private TableColumn<ReviewRow, Integer> colonnaVoto;
    @FXML private TableColumn<ReviewRow, String> colonnaTesto;
    @FXML private Label etichettaVuota;

    private final ObservableList<ReviewRow> dati = FXCollections.observableArrayList();
    private ObservableList<RistoranteDTO> ristoranti;

    public void setRistoranti(ObservableList<RistoranteDTO> ristoranti) {
        this.ristoranti = ristoranti;
    }

    /**
     * Inizializza la tabella, il menu contestuale e carica le recensioni.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() throws IOException {
        colonnaRistorante.setCellValueFactory(new PropertyValueFactory<>("restaurant"));
        colonnaVoto.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colonnaTesto.setCellValueFactory(new PropertyValueFactory<>("text"));

        tabellaRecensioni.setItems(dati);

        menuTastoDestro();

        caricaLeMieRecensioni();
        aggiornaMessaggioVuoto();
    }

    /**
     * Crea il menu contestuale che appare premendo il tasto destro su un riga
     *
     * @author Matteo Franguelli
     */
    private void menuTastoDestro() {
        tabellaRecensioni.setRowFactory(tv -> {
            TableRow<ReviewRow> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem modifyItem = new MenuItem("Modifica recensione");
            MenuItem deleteItem = new MenuItem("Elimina recensione");

            //Modifica
            modifyItem.setOnAction(event -> {
                ReviewRow riga = row.getItem();
                if (riga != null) {
                    try {
                        apriModificaRecensione(riga);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            //Elimina
            deleteItem.setOnAction(event -> {
                ReviewRow rigaSelezionata = row.getItem();
                if (rigaSelezionata != null) {
                    chiediConfermaEdElimina(rigaSelezionata);
                }
            });
            contextMenu.getItems().addAll(modifyItem, deleteItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }
    /**
     * Apre la finestra di modifica per la recensione selezionata.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void apriModificaRecensione(ReviewRow riga) throws IOException {
        RistoranteDTO ristorante = ristoranti == null ? null : ristoranti.stream()
                .filter(r -> r.getIdRistorante() == riga.getRawRestaurantId())
                .findFirst()
                .orElse(null);

        Finestre.apriModale("add_review.fxml", "Modifica Recensione",
                (AddReviewController ctrl) -> {
                    ctrl.setRestaurant(ristorante);
                    ctrl.setRestaurantName(riga.getRestaurant());
                    ctrl.setDatiPerModifica(riga);
                });

        caricaLeMieRecensioni();
    }
    /**
     * Chiede conferma ed elimina la recensione selezionata
     *
     * @author Matteo Franguelli
     */
    private void chiediConfermaEdElimina(ReviewRow riga) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Recensione");
        alert.setHeaderText("Sei sicuro di voler eliminare questa recensione?");
        alert.setContentText("L'operazione e' irreversibile.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            dati.remove(riga);  //rimozione grafica

            rimuoviRecensioneDalFile(riga); //rimozione da file

            aggiornaMessaggioVuoto();
        }
    }

    /**
     * Riscrive il CSV ignorando la riga che corrisponde alla recensione eliminata.
     *
     * @author Matteo Franguelli
     */
    private void rimuoviRecensioneDalFile(ReviewRow riga) {
        Session session = Session.getInstance();
        int mioId = session.getID();
        GestioneFile.rimuoviRecensione(
                mioId,
                riga.getRawRestaurantId(),
                riga.getRating(),
                riga.getText()
        );
    }
    /**
     * Carica dal server tutte le recensioni dell'utente corrente.
     *
     * @author Matteo Franguelli
     * @author Michele Viselli
     * @author Celestino Resteghini
     */
    private void caricaLeMieRecensioni() throws IOException {
        dati.clear();
        Session session = Session.getInstance();
        if (session.isGuest()) return;

        int mioId = session.getID();
        LinkedList<RecensioneDTO> recensioni = (LinkedList<RecensioneDTO>) GestioneRichieste.getInstance().inviaEAttendi("VIS-REC", session.getID());

        for (RecensioneDTO r : recensioni) {
            if (r.getIdUtente() == mioId) {
                String nomeRistorante = r.getNomeRistorante();
                if (nomeRistorante == null || nomeRistorante.isBlank()) {
                    nomeRistorante = "Sconosciuto (ID " + r.getIdRistorante() + ")";
                }
                dati.add(new ReviewRow(nomeRistorante, r.getNumeroStelle(), r.getTesto(), r.getIdRistorante(), r.getIdRecensione()));
            }
        }
    }

    /**
     * Aggiorna la visibilità della tabella e del messaggio di lista vuota.
     *
     * @author Matteo Franguelli
     */
    private void aggiornaMessaggioVuoto() {
        boolean vuota = dati.isEmpty();
        if (etichettaVuota != null) { etichettaVuota.setVisible(vuota); etichettaVuota.setManaged(vuota); }
        if (tabellaRecensioni != null) { tabellaRecensioni.setVisible(!vuota); }
    }

    /**
     * Modello dati per la visualizzazione di una recensione nella tabella.
     *
     * @author Matteo Franguelli
     */
    public static class ReviewRow {
        private final String restaurant;
        private final int rating;
        private final String text;
        private final int rawRestaurantId; // Serve per l'eliminazione
        private final int rawRecensioneId; //Serve per la modifica

        public ReviewRow(String restaurant, int rating, String text, int rawRestaurantId, int rawRecensioneId) {
            this.restaurant = restaurant;
            this.rating = rating;
            this.text = text;
            this.rawRestaurantId = rawRestaurantId;
            this.rawRecensioneId = rawRecensioneId;
        }

        public int getRawRecensioneId() { return rawRecensioneId; }
        public String getRestaurant() { return restaurant; }
        public int getRating() { return rating; }
        public String getText() { return text; }
        public int getRawRestaurantId() { return rawRestaurantId; }
    }
}
