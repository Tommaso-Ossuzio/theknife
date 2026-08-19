package theknife.ui.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//TODO da rivedere, vengono richiamati i file

/**
 * Classe che si occupa della gestione di nuove Recensioni.
 * @author Elia Toschi
 * @author Matteo Franguelli
 * @author Celestino Resteghini
 */
public class AddReviewController {

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_RECENSIONI = "recensioni.csv";
    private static final String NOME_FILE_USER = "users.csv";

    private boolean modalitaModifica = false;
    private MyReviewsController.ReviewRow recensioneOriginale;

    @FXML private Label etichettaTitolo;
    @FXML private TextArea areaRecensione;
    @FXML private Label etichettaErrore;

    // Riferimenti ai 5 bottoni stella
    @FXML private Button star1, star2, star3, star4, star5;

    private Ristorante ristoranteDestinazione;

    // Variabile per tenere traccia del voto (default 5 stelle)
    private int votoSelezionato = 5;

    /**
     * Seleziona il ristorante
     * @author Celestino Resteghini
     * @param restaurant
     */
    public void setRestaurant(Ristorante restaurant) {
        this.ristoranteDestinazione = restaurant;
    }

    /**
     * Seleziona il ristorante dal nome
     * @author Matteo Franguelli
     * @param nomeRistorante
     */
    public void setRestaurantName(String nomeRistorante) {
        if (etichettaTitolo != null && nomeRistorante != null && !nomeRistorante.isBlank()) {
            etichettaTitolo.setText("Recensisci: " + nomeRistorante);
        }
    }

    /**
     * Inizializza le stelle impostate a 5
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        // Appena si apre la finestra, coloriamo le stelle in base al default (5)
        aggiornaGraficaStelle();
    }

    /* =========================
       GESTIONE STELLE
       ========================= */

    /**
     * Gestisce l'azione click delle stelle
     * @author Matteo Franguelli
     * @param event
     */
    @FXML
    private void onStarClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        // Leggiamo "1", "2"... dallo userData definito nell'FXML
        String val = (String) btn.getUserData();
        votoSelezionato = Integer.parseInt(val);

        aggiornaGraficaStelle();
    }

    /**
     * Modifica il numero di stelle selezionate nella grafica
     * @author Matteo Franguelli
     */
    private void aggiornaGraficaStelle() {
        Button[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            // Se l'indice è inferiore al voto selezionato la stella è accesa.
            // Es. voto 3: indici 0, 1, 2 sono accesi.
            // L'aspetto è definito in style.css dalle classi .star-button / .star-button-on
            stars[i].getStyleClass().remove("star-button-on");
            if (i < votoSelezionato) {
                stars[i].getStyleClass().add("star-button-on");
            }
        }
    }
    /* =========================
       AZIONI SALVA / ANNULLA
       ========================= */

    /**
     * gestisce l'operazione di annullamento
     * @author Matteo Franguelli
     */
    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    /**
     * Chiude la finestra
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage finestra = (Stage) areaRecensione.getScene().getWindow();
        finestra.close();
    }

    /**
     * Aggiunge e sceive la recensione su file
     * @author Elia Toschi
     * @author Matteo Franguelli
     */
    @FXML
    private void onCreate() {
        if (modalitaModifica) {
            rimuoviVecchiaEAgungiNuova();
            return;
        }
        GestioneRecensioni gestRest =  GestioneRecensioni.getInstance();
        GestioneRistoranti gr =  GestioneRistoranti.getInstance();
        String utente     = Session.getInstance().getUsername();
        String indirizzoRistorante = ristoranteDestinazione.getLuogo().getIndirizzo();
        int idUtente =1;
        int  stelle       = votoSelezionato;
        String text      = areaRecensione.getText();
        Ristorante r = gr.getRistoranteDaIndirizzo(indirizzoRistorante);
        int idRistorante= r.getId();

        //Prendere id utente
        idUtente=GestioneFile.recuperaId(utente);

        if (stelle == 0 || idUtente== 0 || areaRecensione.getText().isEmpty() || idRistorante== 0) {
            etichettaErrore.setText("Inserire tutti i campi");
            return;
        }

        File fileRecensioni = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        Recensione recensione= new Recensione(stelle,text,Integer.valueOf(idUtente),idRistorante);
        LocalDateTime oraLocale= LocalDateTime.now();
        recensione.setData(Date.from(oraLocale.atZone(ZoneId.systemDefault()).toInstant()));
        gestRest.add(recensione);

        // Salva su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRecensioni, true))) {
            bw.write(stelle + ";" + text + ";" +
                     oraLocale+ ";" +
                    idUtente + ";" +
                    idRistorante);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            etichettaErrore.setText("Errore nel salvataggio recensione su file.");
            return;
        }

        // Il file è cambiato: media e conteggi mostrati nelle card vanno ricalcolati
        gestRest.ricaricaIndice();

        chiudiFinestra();
    }

    /**
     * Metodo usato per modificare una recensione.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    public void setDatiPerModifica(MyReviewsController.ReviewRow recensioneVecchia) {
        this.modalitaModifica = true;
        this.recensioneOriginale = recensioneVecchia;

        // Riempi i campi con i dati vecchi
        areaRecensione.setText(recensioneVecchia.getText());
        votoSelezionato = recensioneVecchia.getRating();
        aggiornaGraficaStelle();

        // Cambia titolo
        if (etichettaTitolo != null) etichettaTitolo.setText("Modifica recensione");
    }

    /**
     * Modifica una recensione
     * @author Matteo Franguelli
     */
    //Todo va aggiunta anche la riscrittura della risposta
    private void rimuoviVecchiaEAgungiNuova() {
        File file = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        List<String> righe = new LinkedList<>();

        // Recupera i dati base
        String user = Session.getInstance().getUsername();
        int mioId = GestioneFile.recuperaId(user);

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) { righe.add(line); continue; }

                // Se la riga corrisponde a quella vecchia, NON aggiungerla (la stiamo cancellando)
                String[] p = line.split(line.contains(";") ? ";" : ",");
                if (p.length >= 5) {
                    try {
                        int idR = Integer.parseInt(p[4].trim());
                        int idU = Integer.parseInt(p[3].trim());
                        String txt = p[1].trim().replace("\"", "");

                        if (idR == recensioneOriginale.getRawRestaurantId() && idU == mioId && txt.equals(recensioneOriginale.getText())) {
                            continue; // SALTA QUESTA RIGA (è quella vecchia)
                        }
                    } catch(Exception e){}
                }
                righe.add(line); // Tieni tutte le altre
            }
        } catch (IOException e) { e.printStackTrace(); }

        // Aggiungi la NUOVA versione in fondo alla lista
        String nuovaRiga = votoSelezionato + ";" + areaRecensione.getText().replace(";", "").replace("\n", " ") + ";" + LocalDateTime.now() + ";" + mioId + ";" + recensioneOriginale.getRawRestaurantId();
        righe.add(nuovaRiga);

        // Riscrivi il file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {}

        // Il file è cambiato: media e conteggi mostrati nelle card vanno ricalcolati
        GestioneRecensioni.getInstance().ricaricaIndice();

        chiudiFinestra();
    }
 //todo verifica e avvia
    public void rispondiRecensione()
    {
        GestioneRecensioni gestRest =  GestioneRecensioni.getInstance();

        File fileRecensioni = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        if (!fileRecensioni.exists()) {
            System.err.println("File utenti non trovato ");

        }
        int stelle=0;
        String text="";
        Date datatemp=null;
        int idUtente=0;
        int idRistorante=0;
        String risposta="";

        risposta = "risposta bella";



        try (BufferedReader br = new BufferedReader(new FileReader(fileRecensioni, StandardCharsets.UTF_8))) {
            String linea = br.readLine(); // Salta header (se presente) o leggilo

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                // Nota: users.csv usa il punto e virgola come separatore
                String[] parti = linea.split(";");


                // Controlliamo che ci siano abbastanza colonne
                if (parti.length > 4) {

                    if(parti[0].trim()=="-1" |parti[1] ==null|parti[2]== null|parti[3]==null| parti[4]==null)
                        continue;

                    stelle=Integer.valueOf(parti[0].trim());
                    text=parti[1].trim();

                    LocalDateTime localDateTime = LocalDateTime.parse(parti[2].trim());
                    datatemp =Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    LocalDateTime data = datatemp.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    idUtente = Integer.valueOf(parti[3].trim());
                    idRistorante= Integer.valueOf(parti[4].trim());
                    // non reinserisco la linea attuale
                     try {
                            int idR = Integer.parseInt(parti[4].trim());
                            int idU = Integer.parseInt(parti[3].trim());
                            String txt = parti[1].trim().replace("\"", "");

                            if (idR == recensioneOriginale.getRawRestaurantId() && idU ==idUtente  && txt.equals(recensioneOriginale.getText())) {
                                continue; // SALTA QUESTA RIGA (è quella vecchia)
                            }
                        } catch(Exception e){}


                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRecensioni, false))) {
                        bw.write(stelle + ";" + text + ";" +
                                data+ ";" +
                                idUtente + ";" +
                                idRistorante+";"+ risposta);
                        bw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        etichettaErrore.setText("Errore nel salvataggio recensione su file.");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





}