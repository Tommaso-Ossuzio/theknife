package theknife.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

//TODO da rivedere, vengono usati i file

/**
 * Si occupa della gestione dei ristoranti di cui l'utente è proprietario.
 * @author Celestino Resteghini
 * version 2
 */
public class MyRestaurantsController {
    @FXML private TableColumn<RistoranteRow, String> colonnaNome;
    @FXML private TableColumn<RistoranteRow, String> colonnaCitta;
    @FXML private TableColumn<RistoranteRow, String> colonnaIndirizzo;
    @FXML private TableColumn<RistoranteRow, String> colonnaCucina;
    @FXML private TableColumn<RistoranteRow, String> colonnaStelle;
    @FXML private TableColumn<RistoranteRow, String> colonnaRecensioni;

    @FXML private TableView<RistoranteRow> tabellaRistoranti;

    private final ObservableList<RistoranteRow> rist = FXCollections.observableArrayList();

    @FXML private Label etichettaVuota;

    @FXML
    private void initialize() {
        colonnaNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colonnaCitta.setCellValueFactory(
                new PropertyValueFactory<>("citta")
        );

        colonnaIndirizzo.setCellValueFactory(
                new PropertyValueFactory<>("indirizzo")
        );

        colonnaCucina.setCellValueFactory(
                new PropertyValueFactory<>("cucina")
        );

        colonnaStelle.setCellValueFactory(
                new PropertyValueFactory<>("stelle")
        );

        colonnaRecensioni.setCellValueFactory(
                new PropertyValueFactory<>("recensioni")
        );

        tabellaRistoranti.setItems(rist);

        aggiornaMessaggioVuoto();
    }

    /**
     * Aggiorna la visibilità del campo grafico "etichettaVuota" quando
     * a tabella dei ristoranti è vuota o contiene elementi.
     * Il messaggio viene mostrato solo se la tabella non
     * contiene alcun ristorante.
     * @author Matteo Franguelli
     */
    private void aggiornaMessaggioVuoto() {
        boolean nessunElemento = rist.isEmpty();

        etichettaVuota.setVisible(nessunElemento);
        etichettaVuota.setManaged(nessunElemento);
        tabellaRistoranti.setVisible(!nessunElemento);
    }

    public static class RistoranteRow {
        private final String nome;
        private final String citta;
        private final String indirizzo;
        private final String cucina;
        private final String stelle;
        private final String recensioni;
        private final int rawRestaurantId; // Serve per l'eliminazione

        public RistoranteRow(String nome, String citta, String indirizzo, String cucina, String stelle, String recensioni, int rawRestaurantId) {
            this.nome = nome;
            this.citta = citta;
            this.indirizzo = indirizzo;
            this.cucina = cucina;
            this.stelle = stelle;
            this.recensioni = recensioni;
            this.rawRestaurantId = rawRestaurantId;
        }

        public String getNome() { return nome; }
        public String getCitta() { return citta; }
        public String getIndirizzo() { return indirizzo; }
        public String getCucina() { return cucina; }
        public String getStelle() { return stelle; }
        public String getRecensioni() { return recensioni; }
        public int getRawRestaurantId() { return rawRestaurantId; }
    }
}