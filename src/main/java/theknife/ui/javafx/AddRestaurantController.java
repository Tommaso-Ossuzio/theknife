package theknife.ui.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.GestioneRistoranti;
import theknife.model.Luogo;
import theknife.model.Ristorante;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

//TODO da rivedere, vengono richiamati i file più volte

/**
 * Controller per la gestione dell'aggiunta di nuovi ristoranti.
 * @author Celestino Resteghini
 */
public class AddRestaurantController {

    // Campi di testo per inserire i dati del ristorante
    @FXML private TextField campoNome;
    @FXML private TextField campoNazione;
    @FXML private TextField campoCitta;
    @FXML private TextField campoIndirizzo;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoLatitudine;
    @FXML private TextField campoLongitudine;
    @FXML private TextField campoPrezzo;
    @FXML private TextField campoTipoCucina;
    @FXML private CheckBox checkConsegna;
    @FXML private CheckBox checkPrenotazione;
    @FXML private TextField campoSitoWeb;

    // Etichetta per mostrare i messaggi di errore all’utente
    @FXML private Label etichettaErrore;

    // Riferimento al controller principale della finestra principale
    private MainController controllerPrincipale;

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE = "michelin_my_maps.csv";
    private static final String NOME_FILE_U = "users.csv";
    GestioneRistoranti gr = GestioneRistoranti.getInstance();

    /**
     * Imposta il controller principale (la finestra da cui è stata aperta questa finestra).
     * Questo ti permette, in futuro, di passare il nuovo ristorante alla lista principale.
     * @author Matteo Franguelli
     */
    public void setControllerPrincipale(MainController controllerPrincipale) {
        this.controllerPrincipale = controllerPrincipale;
    }

    /**
     * Metodo chiamato quando l’utente preme il pulsante "Salva".
     * Qui facciamo un controllo sui dati e poi chiudiamo la finestra.
     * @author Celestino Resteghini
     */
    @FXML
    private void onSalva() {
        MainController controller = new MainController();
        String nome = campoNome.getText();
        String nazione = campoNazione.getText();
        String citta = campoCitta.getText();
        String indirizzo = campoIndirizzo.getText();
        String lat = campoLatitudine.getText();
        String longi = campoLongitudine.getText();
        double prezzo = 0;
        if(!campoPrezzo.getText().isEmpty())
            prezzo = Double.valueOf(campoPrezzo.getText());
        String tipo = campoTipoCucina.getText();
        boolean delivery = checkConsegna.isSelected();
        boolean booking = checkPrenotazione.isSelected();
        String sito = campoSitoWeb.getText();
        String numTel = campoTelefono.getText();
        String stelle="0";

        //TODO: Conversione prezzo
        /*
            € → meno di 35 €
            €€ → tra 35 € e 60 €
            €€€ → tra 60 € e 100 €
            €€€€ → oltre 100 €

            Michelin li descrive anche così:

            € = “per tutte le tasche”
            €€ = “costo ragionevole”
            €€€ = “occasione speciale”
            €€€€ = “piccola follia”
        */
        String p = "";
        if(prezzo <= 35)
            p="€";
        else if(prezzo > 35 && prezzo <= 60)
            p="€€";
        else if(prezzo > 60 && prezzo <= 100)
            p="€€€";
        else if(prezzo > 100)
            p="€€€€";

        // Controllo base: il nome del ristorante è obbligatorio
        if (nome == null || nome.isBlank()) {
            etichettaErrore.setText("Il nome è obbligatorio.");
            return;
        }

        // Verifica cartella
        File cartellaDoc = new File(NOME_CARTELLA);
        if (!cartellaDoc.exists()) {
            boolean creata = cartellaDoc.mkdirs();
            if (!creata) {
                etichettaErrore.setText("Impossibile creare la cartella " + NOME_CARTELLA);
                return;
            }
        }

        File fileUtenti = new File(cartellaDoc, NOME_FILE);

        // Salva su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileUtenti, true))) {
            bw.write(nome + "," + "\"" + indirizzo + ", " + citta + "\"" + "," + "\"" +
                    citta + ", " + nazione + "\"" + "," +
                    p + "," + "\"" +
                    tipo + "\"" + "," +
                    longi + "," +
                    lat + "," +
                    numTel + "," +
                    sito + "," +
                    sito + "," +
                    stelle + " Stars" + "," +
                    null + "," + null + "," + null + "," +
                    delivery + "," + booking);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            etichettaErrore.setText("Errore nel salvataggio ristorante su file.");
            return;
        }

        LinkedList<String> tipoCucina= new LinkedList<>();
        String[] s = tipo.split(",");
        for (String e : s) {
            tipoCucina.add(e.trim());
        }
        Ristorante rist = new Ristorante(nome, numTel, delivery, booking, prezzo, tipoCucina, new Luogo(nazione, indirizzo, citta, Double.valueOf(lat), Double.valueOf(longi)), sito, sito, 0);
        gr.listaRistoranti.add(rist);

        //inserisco il ristorante nel file users.csv
        aggiungiMioRistorante(rist.getId());

        // Avviso l'utente del successo
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Salvataggio effettuato");
        a.setHeaderText(null);
        a.setContentText("Ristorante salvato correttamente.");
        a.showAndWait();

        controllerPrincipale.onResetFilters();
        chiudiFinestra();
    }

    /**
     * Metodo chiamato quando l’utente preme il pulsante "Annulla".
     * Non salva niente, semplicemente chiude la finestra.
     * @author Celestino Resteghini
     */
    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    /**
     * Chiude la finestra corrente.
     * @author Celestino Resteghini
     */
    private void chiudiFinestra() {
        Stage finestra = (Stage) campoNome.getScene().getWindow();
        finestra.close();
    }

    /**
     * Aggiunge il ristorante sia nel file che graficamente
     * @author Celestino Resteghini
     * @param id
     */
    public void aggiungiMioRistorante(int id)
    {
        String usernameU = Session.getInstance().getUsername();
        File fileUtenti = new File(NOME_CARTELLA, NOME_FILE_U);
        if (!fileUtenti.exists()) return;
        List<String> righe = new LinkedList<>();
        String primaparte="";
        String idRistorantiPres="";

        try (BufferedReader lettore = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String linea;

            while ((linea = lettore.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] parti = linea.split(";");

                // Formato CSV atteso: username;hash;nome;cognome;città;isCliente;isRistoratore;RistorantiPreferiti;MieiRistoranti
                if (parti.length >= 2) {
                    if (parti[0].equals(usernameU)) {

                        //Salvo la prima parte della riga
                        if(parti.length > 8)
                            primaparte = parti[0]+";"+parti[1]+";"+parti[2]+";"+parti[3]+";"+parti[4]+";"+parti[5]+";"+parti[6]+";"+parti[7]+";"+parti[8]+";";
                        else
                            primaparte = parti[0]+";"+parti[1]+";"+parti[2]+";"+parti[3]+";"+parti[4]+";"+parti[5]+";"+parti[6]+";"+parti[7]+";"+null+";";
                        if (parti.length > 9) {
                            idRistorantiPres = parti[9].trim();
                            String[] s1 = parti[9].split("-");
                            //Controllo se il ristorante era già presente nei miei ristoranti
                            for(String stringa: s1)
                                if(id == Integer.valueOf(stringa))
                                {
                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Attenzione");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Questo ristorante è già presente nella tua lista.");
                                    alert.showAndWait();
                                    return;
                                }
                            //Salvo il nuovo ristorante
                            idRistorantiPres = idRistorantiPres.trim()+"-"+String.valueOf(id);
                            continue; // SALTA QUESTA RIGA (è quella vecchia)
                        }
                        else
                        {
                            idRistorantiPres = String.valueOf(id);
                            continue; // SALTA QUESTA RIGA (è quella vecchia)
                        }
                    }
                }
                righe.add(linea); // Tieni tutte le altre
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Aggiungi la NUOVA versione in fondo alla lista
        String nuovaRiga = primaparte + idRistorantiPres;
        righe.add(nuovaRiga);

        // Riscrivi il file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileUtenti, StandardCharsets.UTF_8))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {}
    }
}