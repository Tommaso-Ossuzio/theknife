package theknife.ui.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import theknife.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//TODO da rivedere, vengono usati i file

public class ReplyReviewsController {

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_RECENSIONI = "recensioni.csv";

    @FXML private ListView<Recensione> listaRecensioni;

    private List<Integer> mieiRistorantiIds = new ArrayList<>();


    @FXML
    private void initialize() {
        caricaIMieiRistoranti();
        configuraLista();
        caricaRecensioniRicevute();
    }

    private void caricaIMieiRistoranti() {
        Session session = Session.getInstance();
        if (!session.isRistoratore()) return;

        String username = session.getUsername();
        File fileUsers = new File("data", "users.csv");

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileUsers))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(";");
                if (p.length > 0 && p[0].equals(username)) {
                    if (p.length > 9 && !p[9].isBlank()) {
                        String[] ids = p[9].split("-");
                        for (String id : ids) {
                            try { mieiRistorantiIds.add(Integer.parseInt(id.trim())); } catch (Exception e){}
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void caricaRecensioniRicevute() {
        List<Recensione> tutte = GestioneFile.leggiRecensioni();
        List<Recensione> filtrate = new ArrayList<>();

        for (Recensione r : tutte) {
            if (mieiRistorantiIds.contains(r.get_id_Ristorante())) {
                filtrate.add(r);
            }
        }
        listaRecensioni.getItems().setAll(filtrate);
    }

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
            protected void updateItem(Recensione r, boolean empty) {
                super.updateItem(r, empty);

                if (empty || r == null) {
                    setGraphic(null);
                } else {
                    String nomeRist = trovaNomeRistorante(r.get_id_Ristorante());
                    lblRistorante.setText("Ristorante: " + nomeRist);
                    lblStelle.setText("Voto: " + "★".repeat(r.getNumeroStelle()));
                    lblTesto.setText("\"" + r.getText() + "\"");

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
                        System.out.println("RECENSIONE A CUI SI RISPONDE: " + r.getText());
                        System.out.println("VOTO: " + r.getNumeroStelle());
                        System.out.println("NOME RISTORANTE: " + nomeRist);
                        System.out.println("RISPOSTA INVIATA: " + testoRisposta);
                        System.out.println("--------------------------------------------------");

                        //salvaRisposta(r, testoRisposta);

                        areaRisposta.clear();
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "Risposta inviata con successo!");
                        a.showAndWait();
                    });

                    setGraphic(layout);
                }
            }
        });
    }

    private void salvaRisposta(Recensione recensione, String testoRisposta) {
        File fileRisposte = new File("data", "recensioni.csv");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRisposte, true))) {
            String riga = recensione.get_id_Ristorante() + ";" +
                    recensione.getIdUtente() + ";" +
                    "RISPOSTA: " + testoRisposta.replace(";", "").replace("\n", " ");

            bw.write(riga);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String trovaNomeRistorante(int id) {
        GestioneRistoranti gr = GestioneRistoranti.getInstance();
        for (Ristorante r : gr.listaRistoranti) {
            if (r.getId() == id) return r.getNome();
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
    public void rispondiRecensione(Label rist, Label text, TextArea reply, Recensione rec) {
        GestioneRistoranti gr = GestioneRistoranti.getInstance();
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

        GestioneRecensioni.getInstance().ricaricaIndice();
    }
}

