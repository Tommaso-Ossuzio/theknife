package theknife.ui.javafx;

import it.uninsubria.dto.RistoranteDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import theknife.model.Recensione;
import theknife.model.Risposta;
import theknife.model.Ristorante;

import java.io.*;
import java.nio.charset.StandardCharsets;

//TODO da rivedere, vengono usati i file

/**
 * Controller della vista che mostra le recensioni di un ristorante selezionato.
 * @author Matteo Franguelli
 */
public class ViewReviewsController {

    @FXML private Label etichettaTitolo;
    @FXML private ListView<Recensione> listaRecensioni;
    @FXML private Label etichettaMedia;
    @FXML private Label etichettaConteggio;

    private Ristorante ristoranteSelezionato;
    private Integer idRistoranteSelezionato;
    private final ObservableList<Recensione> recensioniData = FXCollections.observableArrayList();

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_RECENSIONI = "recensioni.csv";
    private static final String NOME_FILE_UTENTI = "users.csv";
    private final java.util.Map<Integer, String> utentiAttuali = new java.util.HashMap<>();
    /**
     * Inizializza la lista delle recensioni e la grafica delle celle.
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        listaRecensioni.setItems(recensioniData);
        impostaGraficaCelle();
    }

    /**
     * Imposta il ristorante corrente e carica le relative recensioni.
     * @author Matteo Franguelli
     */
    public void setRestaurant(Ristorante r) {
        this.ristoranteSelezionato = r;
        this.idRistoranteSelezionato = r == null ? null : r.getId();

        if (this.ristoranteSelezionato != null) {
            if (etichettaTitolo != null) {
                etichettaTitolo.setText("Recensioni: " + r.getNome());
            }
            caricaRecensioniSpecifiche();
            calcolaStatistiche();
        }
    }

    /**
     * Imposta il ristorante ricevuto dal nuovo flusso server.
     *
     * @param r ristorante DTO di cui visualizzare le recensioni
     *
     * @author Michele Viselli
     */
    public void setRestaurant(RistoranteDTO r) {
        this.ristoranteSelezionato = null;
        this.idRistoranteSelezionato = r == null ? null : r.getIdRistorante();

        if (r != null) {
            if (etichettaTitolo != null) {
                etichettaTitolo.setText("Recensioni: " + r.getNome());
            }
            caricaRecensioniSpecifiche();
            calcolaStatistiche();
        }
    }

    /**
     * Carica dal file solo le recensioni associate al ristorante selezionato.
     * @author Matteo Franguelli
     */
    private void caricaRecensioniSpecifiche() {
        recensioniData.clear();

        File file = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea = br.readLine(); // Salta header

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] parti;
                if (linea.contains(";")) parti = linea.split(";");
                else parti = linea.split(",");

                if (parti.length >= 5) {
                    try {
                        String sStelle = pulisci(parti[0]);
                        String testo = pulisci(parti[1]);
                        String sIdUtente = pulisci(parti[3]);
                        String sIdRistorante = pulisci(parti[4]);

                        int idRistCsv = Integer.parseInt(sIdRistorante);
                        int idRistAttuale = idRistoranteSelezionato == null
                                ? 0
                                : idRistoranteSelezionato;

                        // Controlliamo se la recensione appartiene al ristorante selezionato
                        if (idRistCsv == idRistAttuale) {
                            int stelle = Integer.parseInt(sStelle);
                            int idUtente = Integer.parseInt(sIdUtente);

                            Recensione rec = new Recensione(stelle, testo, idUtente, idRistCsv);

                            // Lettura della risposta
                            if (parti.length >= 6) {
                                String testoRisposta = pulisci(parti[5]);

                                if (testoRisposta.startsWith("RISPOSTA:")) {
                                    testoRisposta = testoRisposta.substring(9).trim();
                                }

                                if (!testoRisposta.isBlank() && !testoRisposta.equals("null")) {
                                    Risposta rispObj = new Risposta(idRistCsv, testoRisposta);
                                    rec.setRisposta(rispObj);
                                }
                            }

                            recensioniData.add(rec);
                        }

                    } catch (Exception ignored) {
                        // Ignora righe malformate
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pulisce una stringa rimuovendo spazi e separatori non desiderati.
     * @author Matteo Franguelli
     */
    private String pulisci(String s) {
        if (s == null) return "";
        return s.trim().replace(";", "");
    }

    /**
     * Imposta la grafica personalizzata delle celle della lista recensioni.
     * @author Matteo Franguelli
     */
    private void impostaGraficaCelle() {
        listaRecensioni.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Recensione item, boolean empty) {
                super.updateItem(item, empty);

                // Tutto l'aspetto grafico arriva da style.css tramite le styleClass
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox();
                    box.getStyleClass().add("review-item");

                    StringBuilder stelleStr = new StringBuilder();
                    for(int i=0; i<item.getNumeroStelle(); i++) stelleStr.append("★");
                    for(int i=item.getNumeroStelle(); i<5; i++) stelleStr.append("☆");

                    Label lblStelle = new Label(stelleStr.toString());
                    lblStelle.getStyleClass().add("star-display");

                    Label lblTesto = new Label(item.getText());
                    lblTesto.setWrapText(true);
                    lblTesto.setMaxWidth(350);
                    lblTesto.getStyleClass().add("review-text");

                    box.getChildren().addAll(lblStelle, lblTesto);

                    if (item.getRisposta() != null) {
                        VBox boxRisposta = new VBox();
                        boxRisposta.getStyleClass().add("review-reply");
                        VBox.setMargin(boxRisposta, new Insets(10, 0, 0, 0));

                        Label lblTitolo = new Label("Risposta del ristoratore");
                        lblTitolo.getStyleClass().add("review-reply-title");

                        Label lblRisposta = new Label(item.getRisposta().getTextString());
                        lblRisposta.setWrapText(true);
                        lblRisposta.setMaxWidth(330);
                        lblRisposta.getStyleClass().add("review-reply-text");

                        boxRisposta.getChildren().addAll(lblTitolo, lblRisposta);
                        box.getChildren().add(boxRisposta);
                    }
                    // ---------------------------------------------

                    setGraphic(box);
                }
            }
        });
    }

    /**
     * Chiude la finestra corrente.
     * @author Matteo Franguelli
     */
    @FXML
    private void onChiudi() {
        Stage stage = (Stage) etichettaTitolo.getScene().getWindow();
        stage.close();
    }

    private void calcolaStatistiche() {
        if (recensioniData.isEmpty()) {
            if (etichettaMedia != null) etichettaMedia.setText("Media stelle: N/D");
            if (etichettaConteggio != null) etichettaConteggio.setText("Numero di recensioni: N/D");
            return;
        }

        double sommaStelle = 0;
        for (Recensione rec : recensioniData) {
            sommaStelle += rec.getNumeroStelle();
        }
        double media = sommaStelle / recensioniData.size();

        String mediaFormattata = String.format("%.1f", media);
        if (etichettaMedia != null) {
            etichettaMedia.setText("Media stelle: " + mediaFormattata);
        }
        if (etichettaConteggio != null) {
            etichettaConteggio.setText("Numero di recensioni: " + recensioniData.size());
        }
    }
}
