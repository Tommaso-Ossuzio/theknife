package theknife.ui.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.model.GestioneRistoranti;
import theknife.model.Luogo;
import theknife.model.Ristorante;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

//TODO da rivedere, vengono usati i file

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
    GestioneRistoranti gr = GestioneRistoranti.getInstance();

    /**
     * Inizializza la grafica della lista
     * @author Celestino Resteghini
     */
    @FXML
    private void initialize() {
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
     * Si occupa di aggiungere il ristorante passato ai preferiti.
     * @author Celestino Resteghini
     */
    public void addFavorite() {
        preferiti.clear();

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
                        //Se coincide lo username, prendo i ristoranti preferiti
                        if (userN.equals(mioUsername)) {
                            if (parti.length > 8 && !parti[8].isEmpty()) {
                                String[] s = parti[8].split("-");
                                for (String st : s) {
                                    idRistorante = Integer.valueOf(st);
                                    int id = idRistorante;
                                    Ristorante r = gr.listaRistoranti.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                                    if (r != null)
                                        preferiti.add(new RestaurantRow(r.getNome(), r.getLuogo().toString(), idRistorante));
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
     * Rimuove un ID dalla lista dei preferiti nel file users.csv e riscrive il file.
     * @author Matteo Franguelli
     * @param idDaRimuovere
     */
    private void rimuoviPreferitoDalFile(int idDaRimuovere) {
        File file = new File("data", "users.csv");
        if (!file.exists()) return;

        List<String> righeDaSalvare = new LinkedList<>();
        String mioUsername = Session.getInstance().getUsername();
        String idString = String.valueOf(idDaRimuovere);

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;
                // split con -1 serve a preservare campi vuoti alla fine della riga se presenti
                String[] parti = linea.split(";", -1);

                if (parti.length > 0 && parti[0].equals(mioUsername)) {

                    if (parti.length > 8 && !parti[8].isEmpty()) {
                        String[] preferitiAttuali = parti[8].split("-");
                        StringBuilder nuoviPreferiti = new StringBuilder();
                        boolean primo = true;

                        for (String id : preferitiAttuali) {
                            if (!id.trim().equals(idString)) {
                                if (!primo) {
                                    nuoviPreferiti.append("-");
                                }
                                nuoviPreferiti.append(id.trim());
                                primo = false;
                            }
                        }
                        parti[8] = nuoviPreferiti.toString();
                    }

                    String nuovaRiga = String.join(";", parti);
                    righeDaSalvare.add(nuovaRiga);

                } else {
                    righeDaSalvare.add(linea);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (String riga : righeDaSalvare) {
                bw.write(riga);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    chiediConfermaERimuovi(rigaSelezionata);
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
    private void chiediConfermaERimuovi(RestaurantRow riga) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rimuovi Preferito");
        alert.setHeaderText("Rimuovere " + riga.getNome() + " dai preferiti?");
        alert.setContentText("L'operazione non può essere annullata.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            preferiti.remove(riga);
            rimuoviPreferitoDalFile(riga.getRawRestaurantId());
            aggiornaMessaggioVuoto();
        }
    }
}