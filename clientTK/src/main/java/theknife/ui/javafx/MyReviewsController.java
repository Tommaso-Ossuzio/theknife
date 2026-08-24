package theknife.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.model.GestioneFile;
import theknife.model.GestioneRistoranti;
import theknife.model.Recensione;
import theknife.model.Ristorante;
import theknife.utilities.Finestre;

import java.util.List;

//TODO da rivedere

/**
 * Controller che gestisce la visualizzazione e la gestione
 * delle recensioni scritte dall'utente corrente.
 *
 * @author Matteo Franguelli
 */
public class MyReviewsController {

    @FXML private TableView<ReviewRow> tabellaRecensioni;
    @FXML private TableColumn<ReviewRow, String> colonnaRistorante;
    @FXML private TableColumn<ReviewRow, Integer> colonnaVoto;
    @FXML private TableColumn<ReviewRow, String> colonnaTesto;
    @FXML private Label etichettaVuota;

    private final ObservableList<ReviewRow> dati = FXCollections.observableArrayList();

    /**
     * Inizializza la tabella, il menu contestuale e carica le recensioni.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
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
                    apriModificaRecensione(riga);
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
     *
     * @author Matteo Franguelli
     */
    private void apriModificaRecensione(ReviewRow riga) {
        GestioneRistoranti gr = GestioneRistoranti.getInstance();
        Ristorante r = gr.getRistorante(riga.getRawRestaurantId());

        Finestre.apriModale("add_review.fxml", "Modifica Recensione",
                (AddReviewController ctrl) -> {
                    ctrl.setRestaurant(r);
                    if (r != null) ctrl.setRestaurantName(r.getNome());
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
        int mioId = GestioneFile.recuperaId(session.getUsername());
        GestioneFile.rimuoviRecensione(
                mioId,
                riga.getRawRestaurantId(),
                riga.getRating(),
                riga.getText()
        );
    }
    /**
     * Carica dal file tutte le recensioni dell'utente corrente.
     *
     * @author Matteo Franguelli
     */
    private void caricaLeMieRecensioni() {
        dati.clear();
        Session session = Session.getInstance();
        if (session.isGuest()) return;

        int mioId = GestioneFile.recuperaId(session.getUsername());
        List<Recensione> tutteLeRecensioni = GestioneFile.leggiRecensioni();

        for (Recensione r : tutteLeRecensioni) {
            if (r.getIdUtente() == mioId) {
                String nomeRistorante = trovaNomeRistorante(r.get_id_Ristorante());
                dati.add(new ReviewRow(nomeRistorante, r.getNumeroStelle(), r.getText(), r.get_id_Ristorante()));
            }
        }
    }
    /**
     * Restituisce il nome del ristorante dato il suo id.
     * @param idRistorante
     * @author Matteo Franguelli
     */
    private String trovaNomeRistorante(int idRistorante) {
        GestioneRistoranti gr = GestioneRistoranti.getInstance();
        for (Ristorante r : gr.getListaRistoranti()) {
            if (r.getId() == idRistorante) return r.getNome();
        }
        return "Ristorante ID: " + idRistorante;
    }
    /**
     * Pulisce una stringa da caratteri non desiderati.
     * @param s
     * @author Matteo Franguelli
     */
    private String pulisci(String s) {
        if (s == null) return "";
        return s.trim().replace(";", "").replace("\"", "");
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

        public ReviewRow(String restaurant, int rating, String text, int rawRestaurantId) {
            this.restaurant = restaurant;
            this.rating = rating;
            this.text = text;
            this.rawRestaurantId = rawRestaurantId;
        }

        public String getRestaurant() { return restaurant; }
        public int getRating() { return rating; }
        public String getText() { return text; }
        public int getRawRestaurantId() { return rawRestaurantId; }
    }
}