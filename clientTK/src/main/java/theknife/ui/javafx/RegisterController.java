package theknife.ui.javafx;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.UtenteDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.utilities.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Controller della finestra di registrazione.
 * Gestisce la creazione di un nuovo utente e il salvataggio
 * delle credenziali nel DB
 * @author Matteo Franguelli
 * @author Celestino Resteghini
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
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    @FXML
    private void onCreate(ActionEvent event) throws IOException {
        String nome         = campoNome.getText();
        String cognome      = campoCognome.getText();
        String email     = campoUsername.getText();
        String password     = campoPassword.getText();
        String citta        = campoCitta.getText();
        LocalDate dataN = campoDataNascita.getValue();

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

        String passwordHashed = Utility.calcolaSha256(password);

        Date dataNascita;

        if(dataN == null) {
            dataNascita = null;
        } else {
            dataNascita = Date.from(dataN.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        GestioneRichieste gr = new GestioneRichieste();
        UtenteDTO utente = new UtenteDTO(nome,cognome,email,dataNascita, new LuogoDTO(new CittaDTO(citta)), passwordHashed);
        Boolean risposta = (Boolean) gr.inviaEAttendi("REG",utente);

        // true se la registrazione è andata a buon fine
        if(risposta) {
            // Auto login
            Session.Role ruoloSessione;
            if (isRistoratore) {
                ruoloSessione = Session.Role.RISTORATORE;
            } else {
                ruoloSessione = Session.Role.CLIENTE;
            }

            Session.getInstance().login(email, ruoloSessione);
            //Permessi impostati dopo la registrazione
            Session.getInstance().setPermessi(isCliente, isRistoratore);

            if (controllerPrincipale != null) {
                controllerPrincipale.onLoginSuccess();
            }

            chiudiFinestra();

        } else {
            etichettaErrore.setText("Email già in uso. Usane un'altra.");
            return;
        }
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
     * Chiude la finestra di registrazione.
     *
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage stage = (Stage) campoUsername.getScene().getWindow();
        stage.close();
    }
}