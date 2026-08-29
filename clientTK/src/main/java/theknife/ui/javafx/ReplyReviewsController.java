package theknife.ui.javafx;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RistoranteDTO;
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

                        rispondiRecensione(lblRistorante,lblTesto,areaRisposta,r);

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
     * permette di scrivere la risposta ad una recensione
     * @param rist
     * @param text
     * @param reply
     * @param rec
     * @author Elia Toschi
     */
    public void rispondiRecensione(Label rist, Label text, TextArea reply, RecensioneDTO rec) {
        //TODO aggiungere RISP-REC e poi gestire la cancellazione nella lista
        /*GestioneRistoranti gr = GestioneRistoranti.getInstance();
        File fileRecensioni = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        if (!fileRecensioni.exists()) {
            System.err.println("File utenti non trovato ");
            return;
        }

        int targetStelle = rec.getNumeroStelle();
        String targetTesto = rec.getText();
        int targetIdUtente = rec.getIdUtente();
        int targetIdRist = rec.get_id_Ristorante();
        Date targetData = rec.getData();
        String nuovaRisposta = reply.getText().replace(";", ",").replace("\n", " ");

        LinkedList<Recensione> lista = new LinkedList<>();
        String header = "";

        try (BufferedReader br = new BufferedReader(new FileReader(fileRecensioni, StandardCharsets.UTF_8))) {
            header = br.readLine();
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] parti = linea.split(";");

                if (parti.length > 4) {
                    int currentStelle = Integer.parseInt(parti[0].trim());
                    String currentTesto = parti[1].trim();
                    LocalDateTime ldt = LocalDateTime.parse(parti[2].trim());
                    Date currentData = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    int currentIdUtente = Integer.parseInt(parti[3].trim());
                    int currentIdRistorante = Integer.parseInt(parti[4].trim());

                    if (currentStelle == targetStelle &&
                            currentTesto.equals(targetTesto) &&
                            currentIdUtente == targetIdUtente &&
                            currentIdRistorante == targetIdRist) {

                        if (targetData == null) targetData = currentData;
                        continue;
                    }

                    Recensione rec_temp = new Recensione(currentStelle, currentTesto, currentIdUtente, currentIdRistorante);
                    rec_temp.setData(currentData);

                    String usernameRist= Session.getInstance().getUsername();
                    int idRistoratore=GestioneFile.recuperaId(usernameRist);
                    if (parti.length > 5 && !parti[5].isBlank()) {
                        rec_temp.setRisposta(new Risposta(idRistoratore,parti[5].trim()));
                    }
                    lista.add(rec_temp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String usernameRist= Session.getInstance().getUsername();

        int idRistoratore=GestioneFile.recuperaId(usernameRist);

        Recensione recensioneModificata = new Recensione(targetStelle, targetTesto, targetIdUtente, targetIdRist);
        recensioneModificata.setData(targetData != null ? targetData : new Date());
        recensioneModificata.setRisposta(new Risposta(idRistoratore,  nuovaRisposta));
        lista.add(recensioneModificata);


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRecensioni, false))) {
            if (header != null && !header.isBlank()) {
                bw.write(header);
                bw.newLine();
            }

            for (Recensione r : lista) {
                LocalDateTime ldt = r.getData().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                String stringaRisposta = "";

                if (r.getRisposta() != null && r.getRisposta().getTextString() != null) {
                    stringaRisposta = r.getRisposta().getTextString();
                }
                bw.write(r.getNumeroStelle() + ";" +
                        r.getText() + ";" +
                        ldt.toString() + ";" +
                        r.getIdUtente() + ";" +
                        r.get_id_Ristorante() + ";" +
                        stringaRisposta);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GestioneRecensioni.getInstance().ricaricaIndice();*/
    }
}

