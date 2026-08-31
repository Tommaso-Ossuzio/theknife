/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RispostaDTO;
import it.uninsubria.dto.RistoranteDTO;
import it.uninsubria.dto.RistoratoreDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import theknife.model.*;

import java.io.*;
import java.util.LinkedList;

/**
 * Controller per le risposte alle recensioni.
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * @author Elia Toschi
 */
public class ReplyReviewsController {

    @FXML private ListView<RecensioneDTO> listaRecensioni;

    LinkedList<RistoranteDTO> ristoranti = new LinkedList<>();


    @FXML
    private void initialize() throws IOException {
        caricaIMieiRistoranti();
        configuraLista();
        caricaRecensioniRicevute();
    }

    /**
     * Metodo per ottenere i ristoranti del ristoratore loggato
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     * @throws IOException
     */
    private void caricaIMieiRistoranti() throws IOException {
        Session session = Session.getInstance();
        ristoranti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("RIST", session.getID());
    }

    /**
     * Metodo per caricare la lista delle recensioni senza risposta
     * @author Celestino Resteghini
     * @throws IOException
     */
    private void caricaRecensioniRicevute() throws IOException {
        listaRecensioni.getItems().clear();
        Session session = Session.getInstance();
        LinkedList<RecensioneDTO> lista = (LinkedList<RecensioneDTO>) GestioneRichieste.getInstance().inviaEAttendi("REC-NO-RISP", session.getID());
        listaRecensioni.getItems().addAll(lista);
    }

    /**
     * Metodo per configurare la lista che conterrà le recensioni senza risposta
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    private void configuraLista() {
        listaRecensioni.setCellFactory(lv -> new ListCell<>() {

            private final Label lblRistorante = new Label();
            private final Label lblStelle = new Label();
            private final Label lblTesto = new Label();
            private final TextArea areaRisposta = new TextArea();
            private final Button btnInvia = new Button("Invia Risposta");
            private final VBox layout = new VBox(8);

            {
                lblRistorante.getStyleClass().add("review-title");
                lblStelle.getStyleClass().add("star-display");
                lblTesto.setWrapText(true);
                lblTesto.getStyleClass().add("review-quote");

                areaRisposta.setPromptText("Scrivi qui la tua risposta pubblica...");
                areaRisposta.setPrefRowCount(2);
                areaRisposta.setWrapText(true);

                btnInvia.getStyleClass().addAll("btn-primary", "btn-small");

                layout.getStyleClass().add("review-item");
                layout.getChildren().addAll(lblRistorante, lblStelle, lblTesto, new Separator(), areaRisposta, btnInvia);
            }

            @Override
            protected void updateItem(RecensioneDTO r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setGraphic(null);
                } else {
                    String nomeRist = "";
                    try {
                        nomeRist = trovaNomeRistorante(r.getIdRistorante());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    lblRistorante.setText("Ristorante: " + nomeRist);
                    lblStelle.setText("Voto: " + "★".repeat(r.getNumeroStelle()));
                    lblTesto.setText("\"" + r.getTesto() + "\"");

                    btnInvia.setOnAction(e -> {
                        String testoRisposta = areaRisposta.getText();
                        if (testoRisposta.isBlank()) {
                            Alert a = new Alert(Alert.AlertType.WARNING, "Scrivi una risposta prima di inviare.");
                            a.showAndWait();
                            return;
                        }

                        try {
                            rispondiRecensione(r, testoRisposta);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        // stampa per toschi
                        System.out.println("--------------------------------------------------");
                        System.out.println("RECENSIONE A CUI SI RISPONDE: " + r.getTesto());
                        System.out.println("VOTO: " + r.getNumeroStelle());
                        System.out.println("ID RISTORANTE: " + r.getIdRistorante());
                        System.out.println("RISPOSTA INVIATA: " + testoRisposta);
                        System.out.println("--------------------------------------------------");

                        areaRisposta.clear();
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Risposta inviata con successo!");
                        a.showAndWait();
                    });

                    setGraphic(layout);
                }
            }
        });
    }

    /**
     * Metodo che restituisce il nome del ristorante
     * @param id
     * @return nome del ristorante
     * @throws IOException
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private String trovaNomeRistorante(int id) throws IOException {
        for (RistoranteDTO r : ristoranti)
        {
            if(r.getIdRistorante() == id) return r.getNome();
        }
        return "Sconosciuto (ID " + id + ")";
    }

    @FXML
    private void onChiudi() {
        Stage stage = (Stage) listaRecensioni.getScene().getWindow();
        stage.close();
    }

    /**
     * Aggiunge nel db la risposta alla recensione e toglie quest'ultima dalla lista delle recensioni senza risposta
     * @author Elia Toschi
     * @author Celestino Resteghini
     * @param rec
     * @param testo
     * @throws IOException
     */
    public void rispondiRecensione(RecensioneDTO rec, String testo) throws IOException {
        RispostaDTO risposta = new RispostaDTO(testo, rec.getIdRecensione(), new RistoratoreDTO(rec.getIdUtente()));
        GestioneRichieste.getInstance().inviaSolo("RISP-REC", risposta);
        listaRecensioni.getItems().remove(rec);
    }
}

