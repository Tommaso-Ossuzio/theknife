package theknife.ui.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.utilities.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO da rivedere, vengono usati i file

/**
 * Controller della finestra di registrazione.
 * Gestisce la creazione di un nuovo utente e il salvataggio
 * delle credenziali nel file data/users.csv.
 * @author Matteo Franguelli
 */
public class RegisterController {

    @FXML private TextField campoNome;
    @FXML private TextField campoCognome;
    @FXML private TextField campoUsername;
    @FXML private PasswordField campoPassword;
    @FXML private TextField campoCitta;
    @FXML private DatePicker campoDataNascita;

    @FXML private RadioButton radioCliente;
    @FXML private RadioButton radioRistoratore;

    @FXML private Label etichettaErrore;
    @FXML private Label erroreNome;
    @FXML private Label erroreCognome;
    @FXML private Label erroreUsername;
    @FXML private Label errorePassword;
    @FXML private Label erroreCitta;
    @FXML private Button bottoneCrea;

    private ControllerAutenticazione controllerPrincipale;

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE = "users.csv";
    /**
     * Imposta il controller principale come riferimento.
     *
     * @author Matteo Franguelli
     */
    public void setParentController(ControllerAutenticazione parentController) {
        this.controllerPrincipale = parentController;
    }
    /**
     * Inizializza i valori di default della schermata.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        if (radioCliente != null) {
            radioCliente.setSelected(true);
        }

        Utility.confermaConInvio(bottoneCrea);
    }

    /**
     * Rende cliccabile l'intera card del ruolo "Cliente", non solo il pallino.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onScegliCliente() {
        radioCliente.setSelected(true);
    }

    /**
     * Rende cliccabile l'intera card del ruolo "Ristoratore", non solo il pallino.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onScegliRistoratore() {
        radioRistoratore.setSelected(true);
    }

    /**
     * Gestisce il ritorno alla schermata precedente.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onBack(ActionEvent event) {
        chiudiFinestra();
    }
    /**
     * Gestisce la creazione di un nuovo utente.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onCreate(ActionEvent event) {
        String nome         = campoNome.getText();
        String cognome      = campoCognome.getText();
        String username     = campoUsername.getText();
        String password     = campoPassword.getText();
        String citta        = campoCitta.getText();
        //TODO implementare inserimento nel DB per data di nascita

        // Il ruolo è esclusivo: uno dei due è sempre e solo vero
        boolean isRistoratore = radioRistoratore.isSelected();
        boolean isCliente     = !isRistoratore;

        // Cambi obbligatori
        boolean nomeVuoto     = segnalaSeVuoto(campoNome, erroreNome);
        boolean cognomeVuoto  = segnalaSeVuoto(campoCognome, erroreCognome);
        boolean emailVuota    = segnalaSeVuoto(campoUsername, erroreUsername);
        boolean passwordVuota = segnalaSeVuoto(campoPassword, errorePassword);
        boolean cittaVuota    = segnalaSeVuoto(campoCitta, erroreCitta);

        if (nomeVuoto || cognomeVuoto || emailVuota || passwordVuota || cittaVuota) {
            etichettaErrore.setText("");
            return;
        }

        //TODO implementare controllo indirizzo mail

        // Controllo se username gia' presente
        if (usernameEsiste(username)) {
            etichettaErrore.setText("Email già in uso. Usane un'altra.");
            return;
        }

        String passwordHashed = calcolaSha256(password);

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

        int nuovoId = calcolaProssimoId(fileUtenti);

        // Salva su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileUtenti, true))) {
            bw.write(username + ";" + passwordHashed + ";" +
                    valoreNonNullo(nome) + ";" +
                    valoreNonNullo(cognome) + ";" +
                    valoreNonNullo(citta) + ";" +
                    isCliente + ";" +
                    isRistoratore + ";" +
                    nuovoId);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            etichettaErrore.setText("Errore nel salvataggio utente su file.");
            return;
        }

        // Auto login
        Session.Role ruoloSessione;
        if (isRistoratore) {
            ruoloSessione = Session.Role.RISTORATORE;
        } else {
            ruoloSessione = Session.Role.CLIENTE;
        }

        Session.getInstance().login(username, ruoloSessione);
        //Permessi impostati dopo la registrazione
        Session.getInstance().setPermessi(isCliente, isRistoratore);

        if (controllerPrincipale != null) {
            controllerPrincipale.onLoginSuccess();
        }

        chiudiFinestra();
    }

    /**
     * Scrive "Obbligatorio" di fianco al campo se è rimasto vuoto.
     * @param campo campo obbligatorio da controllare
     * @param errore etichetta accanto al campo
     * @return true se il campo è vuoto
     * @author Matteo Franguelli
     */
    private boolean segnalaSeVuoto(TextField campo, Label errore) {
        boolean vuoto = campo.getText() == null || campo.getText().isBlank();
        errore.setText(vuoto ? "Obbligatorio" : "");
        return vuoto;
    }

    /**
     * Legge il file users.csv per verificare se lo username è già presente.
     * Restituisce true se lo trova, false altrimenti.
     *
     * @author Matteo Franguelli
     */
    private boolean usernameEsiste(String usernameDaCercare) {
        File fileUtenti = new File(NOME_CARTELLA, NOME_FILE);

        // Se il file non esiste ancora non esiste nemmeno lo username
        if (!fileUtenti.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] parti = linea.split(";");
                if (parti.length >= 1) {
                    String usernameSalvato = parti[0];
                    if (usernameSalvato.equalsIgnoreCase(usernameDaCercare)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Chiude la finestra di registrazione.
     *
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage stage = (Stage) campoUsername.getScene().getWindow();
        stage.close();
    }
    /**
     * Calcola l'hash SHA-256 di una stringa.
     *
     * @author Matteo Franguelli
     */
    private String calcolaSha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Calcola il prossimo ID disponibile per un nuovo utente.
     *
     * @author Matteo Franguelli
     */
    private int calcolaProssimoId(File fileUtenti) {
        if (!fileUtenti.exists()) {     return 1;   }

        int maxId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {

                String[] parti = linea.split(";");
                if (parti.length >= 8) {
                    try {
                        int idLetto = Integer.parseInt(parti[parti.length - 1].trim());
                        if (idLetto > maxId) {
                            maxId = idLetto;
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }
    /**
     * Restituisce una stringa non nulla per il salvataggio.
     *
     * @author Matteo Franguelli
     */
    private String valoreNonNullo(String s) {
        return s == null ? "" : s.trim();
    }
}