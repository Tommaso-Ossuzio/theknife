package theknife.ui.javafx;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RistoranteDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * Controller della vista che mostra le recensioni di un ristorante selezionato.
 * @author Michele Viselli
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 */
public class ViewReviewsController {

    @FXML private Label etichettaTitolo;
    @FXML private ListView<RecensioneDTO> listaRecensioni;
    @FXML private Label etichettaMedia;
    @FXML private Label etichettaConteggio;

    private Integer idRistoranteSelezionato;
    private final ObservableList<RecensioneDTO> recensioniData = FXCollections.observableArrayList();

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
     * Imposta il ristorante ricevuto dal server.
     * @param r ristorante DTO di cui visualizzare le recensioni
     * @author Michele Viselli
     * @author Celestino Resteghini
     */
    public void setRestaurant(RistoranteDTO r) throws IOException {
        impostaRistorante(
                r == null ? null : r.getIdRistorante(),
                r == null ? null : r.getNome()
        );
    }

    /**
     * Imposta i dati comuni ai due tipi di ristorante ancora presenti nel
     * client e avvia il caricamento delle recensioni dal server.
     * @param idRistorante identificativo del ristorante
     * @param nomeRistorante nome da mostrare nel titolo
     * @author Michele Viselli
     * @author Celestino Resteghini
     */
    private void impostaRistorante(Integer idRistorante, String nomeRistorante) throws IOException {
        idRistoranteSelezionato = idRistorante;

        if (idRistorante == null) return;

        if (etichettaTitolo != null) {
            etichettaTitolo.setText("Recensioni: " + nomeRistorante);
        }

        caricaRecensioniSpecifiche();
    }

    /**
     * Richiede al server le recensioni del ristorante selezionato senza
     * bloccare il thread JavaFX.
     *
     * @author Michele Viselli
     * @author Matteo Franguelli
     */
    private void caricaRecensioniSpecifiche() {
        recensioniData.clear();
        calcolaStatistiche();

        if (idRistoranteSelezionato == null) return;

        listaRecensioni.setDisable(true);
        impostaPlaceholder("Caricamento recensioni...");

        int idRistorante = idRistoranteSelezionato;
        Task<List<RecensioneDTO>> richiesta = new Task<>() {
            @Override
            protected List<RecensioneDTO> call() {
                return getRecensioniPerRistorante(idRistorante);
            }
        };

        richiesta.setOnSucceeded(evento -> {
            listaRecensioni.setDisable(false);
            List<RecensioneDTO> recensioni = richiesta.getValue();
            if (recensioni != null) {
                recensioniData.setAll(recensioni);
            }
            impostaPlaceholder("Non ci sono ancora recensioni.");
            calcolaStatistiche();
        });

        richiesta.setOnFailed(evento -> {
            listaRecensioni.setDisable(false);
            recensioniData.clear();
            impostaPlaceholder("Impossibile caricare le recensioni.");
            calcolaStatistiche();
        });

        Thread thread = new Thread(richiesta, "recensioni-ristorante");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Aggiorna il messaggio mostrato quando la lista non contiene elementi.
     *
     * @param testo messaggio da mostrare
     * @author Michele Viselli
     */
    private void impostaPlaceholder(String testo) {
        Label placeholder = new Label(testo);
        placeholder.getStyleClass().add("muted-text");
        listaRecensioni.setPlaceholder(placeholder);
    }

    /**
     * Imposta la grafica personalizzata delle celle della lista recensioni.
     * @author Matteo Franguelli
     * @author Michele Viselli
     * @author Elia Toschi
     */
    private void impostaGraficaCelle() {
        listaRecensioni.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(RecensioneDTO item, boolean empty) {
                super.updateItem(item, empty);

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

                    Label lblTesto = new Label(item.getTesto());
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

                        Label lblRisposta = new Label(item.getRisposta().getTesto());
                        lblRisposta.setWrapText(true);
                        lblRisposta.setMaxWidth(330);
                        lblRisposta.getStyleClass().add("review-reply-text");

                        boxRisposta.getChildren().addAll(lblTitolo, lblRisposta);
                        box.getChildren().add(boxRisposta);
                    }

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

    /**
     * imposta le statistiche
     * @author Matteo Fraqnguelli
     * @author Michele Viselli
     */
    private void calcolaStatistiche() {
        if (recensioniData.isEmpty()) {
            if (etichettaMedia != null) etichettaMedia.setText("Media stelle: N/D");
            if (etichettaConteggio != null) etichettaConteggio.setText("Numero di recensioni: N/D");
            return;
        }

        double sommaStelle = 0;
        for (RecensioneDTO rec : recensioniData) {
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

    /**
     * Richiede al server le recensioni associate a un ristorante.
     *
     * <p>Il metodo è bloccante perché attende la risposta della socket; deve
     * quindi essere richiamato da un thread diverso da quello JavaFX.</p>
     *
     * @param idRistorante identificativo del ristorante
     * @return recensioni ricevute dal server, oppure una lista vuota se la
     *         risposta non è disponibile o la connessione fallisce
     * @author Michele Viselli
     * @author Celestino Resteghini
     */
    public LinkedList<RecensioneDTO> getRecensioniPerRistorante(int idRistorante) {
        try {
            Object risposta = GestioneRichieste.getInstance()
                    .inviaEAttendi("REC", idRistorante);

            if (risposta instanceof List<?>) {
                LinkedList<RecensioneDTO> risultato = new LinkedList<>();

                for (Object elemento : (List<?>) risposta) {
                    if (elemento instanceof RecensioneDTO) {
                        risultato.add((RecensioneDTO) elemento);
                    }
                }
                return risultato;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new LinkedList<>();
    }
}
