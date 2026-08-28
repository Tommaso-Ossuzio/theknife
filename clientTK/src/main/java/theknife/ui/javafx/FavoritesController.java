package theknife.ui.javafx;

import it.uninsubria.dto.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.model.GestioneRichieste;
import theknife.model.GestioneRistoranti;
import theknife.model.Luogo;
import theknife.model.Ristorante;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe che si occupa della gestione dei ristoranti preferiti.
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * version 2
 *
 */
public class FavoritesController {

    @FXML private TableColumn<RestaurantRow, String> colonnaRistorante;
    @FXML private TableColumn<RestaurantRow, String> colonnaLuogo;
    @FXML private TableView<RestaurantRow> tabellaPreferiti;
    @FXML private Label etichettaVuota;

    private final ObservableList<RestaurantRow> preferiti = FXCollections.observableArrayList();

    /**
     * Inizializza la grafica della lista
     * @author Celestino Resteghini
     */
    @FXML
    private void initialize() throws IOException {
        colonnaRistorante.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colonnaLuogo.setCellValueFactory(
                new PropertyValueFactory<>("luogo")
        );

        tabellaPreferiti.setItems(preferiti);
        menuTastoDestro();
        addFavorite();
        aggiornaMessaggioVuoto();
    }

    /**
     * Si occupa di aggiungere la lista di ristoranti preferiti nella TableView.
     * @author Celestino Resteghini
     */
    public void addFavorite() throws IOException {
        Session session = Session.getInstance();
        if (session.isGuest()) return;

        int idUtente = session.getID();

        LinkedList<RistoranteDTO> ristoranti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("VIS-PREF", idUtente);

        preferiti.clear();

        if (ristoranti != null) {
            for (RistoranteDTO r : ristoranti) {
                String luogoFormattato = r.getLuogo().getVia() + ", " + r.getLuogo().getCitta().getNazione();
                preferiti.add(new RestaurantRow(r.getNome(), luogoFormattato, r.getIdRistorante()));
            }
        }
    }

    /**
     * Metodo per rimuovere un ristorante preferito dal db
     * @author Celestino Resteghini
     * @param idDaRimuovere
     * @throws IOException
     */
    private void rimuoviPreferito(int idDaRimuovere) throws IOException {
        Session session = Session.getInstance();
        HashMap<String, Integer> id = new HashMap<>();
        int idUtente = session.getID();
        id.put("idUtente", idUtente);
        id.put("idRistorante", idDaRimuovere);
        GestioneRichieste.getInstance().inviaSolo("ELIM-PREF", id);
    }


    /**
     * Si occupa di mostrare l'etichetta che indica i ristoranti preferiti.
     * @author Matteo Franguelli
     */
    private void aggiornaMessaggioVuoto() {
        boolean nessunElemento = preferiti.isEmpty();

        etichettaVuota.setVisible(nessunElemento);
        etichettaVuota.setManaged(nessunElemento);
        tabellaPreferiti.setVisible(!nessunElemento);
    }

    public static class RestaurantRow {
        private final String nome;
        private final String luogo;
        private final int rawRestaurantId; // Serve per l'eliminazione

        public RestaurantRow(String nome, String luogo, int rawRestaurantId) {
            this.nome = nome;
            this.luogo = luogo;
            this.rawRestaurantId = rawRestaurantId;
        }

        public String getNome() { return nome; }
        public String getLuogo() { return luogo; }
        public int getRawRestaurantId() { return rawRestaurantId; }
    }

    /**
     * Crea il menu contestuale (tasto destro) con la sola opzione Elimina.
     * @author Matteo Franguelli
     */
    private void menuTastoDestro() {
        tabellaPreferiti.setRowFactory(tv -> {
            TableRow<RestaurantRow> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Rimuovi dai preferiti");

            deleteItem.setOnAction(event -> {
                RestaurantRow rigaSelezionata = row.getItem();
                if (rigaSelezionata != null) {
                    try {
                        chiediConfermaERimuovi(rigaSelezionata);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            contextMenu.getItems().add(deleteItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Chiede, tramite un popup di conferma, se la rimozione e' volontaria
     * @author Matteo Franguelli
     */
    private void chiediConfermaERimuovi(RestaurantRow riga) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rimuovi Preferito");
        alert.setHeaderText("Rimuovere " + riga.getNome() + " dai preferiti?");
        alert.setContentText("L'operazione non può essere annullata.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            preferiti.remove(riga);
            rimuoviPreferito(riga.getRawRestaurantId());
            aggiornaMessaggioVuoto();
        }
    }
}