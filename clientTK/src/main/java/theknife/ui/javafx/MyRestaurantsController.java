package theknife.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.model.GestioneRecensioni;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    GestioneRistoranti gr = GestioneRistoranti.getInstance();

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

        setRestaurants();

        aggiornaMessaggioVuoto();
    }

    /**
     * Aggiorna il contenuto della tabella dei ristoranti nella vista.
     * @author Celestino Resteghini
     */
    public void setRestaurants() {
        rist.clear();

        Session session = Session.getInstance();
        if (session.isGuest()) return;

        String mioUsername = session.getUsername();
        int idRistorante=0;

        File file = new File("data", "users.csv");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea = br.readLine();

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] parti = linea.contains(";") ? linea.split(";") : linea.split(",");

                if (parti.length >= 0) {
                    try {
                        String userN = parti[0].trim();
                        //Se coincide lo username, prendo i miei ristoranti
                        if (userN.equals(mioUsername)) {
                            if (parti.length > 9 && !parti[9].isEmpty()) {
                                String[] s = parti[9].split("-");
                                for (String st : s) {
                                    idRistorante = Integer.valueOf(st);
                                    int id = idRistorante;
                                    Ristorante r = gr.listaRistoranti.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                                    if (r != null) {
                                        String ste;
                                        if(r.getMediaStelle()==-1)
                                            ste= "Nessuna recensione";
                                        else
                                            ste = String.valueOf(r.getMediaStelle()) + " ⭐";
                                        rist.add(new RistoranteRow(r.getNome(), r.getLuogo().getCitta(), r.getLuogo().getIndirizzo(), r.getStringaCucina(), ste, String.valueOf(r.getNumRecensioni()), idRistorante));
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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