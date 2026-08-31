/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.UtenteDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;
import theknife.model.GestioneRichieste;
import theknife.utilities.Utility;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
    @FXML private ComboBox<String> campoCitta;
    @FXML private DatePicker campoDataNascita;

    @FXML private RadioButton radioCliente;
    @FXML private RadioButton radioRistoratore;

    @FXML private Label etichettaErrore;
    @FXML private Label erroreNome;
    @FXML private Label erroreCognome;
    @FXML private Label erroreUsername;
    @FXML private Label errorePassword;
    @FXML private Label erroreCitta;
    @FXML private Label erroreDataNascita;
    @FXML private Button bottoneCrea;

    private ControllerAutenticazione controllerPrincipale;
    private boolean testoDataNascitaNonValido;

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
        Utility.completamentoCitta(campoCitta);
        Utility.confermaConInvio(bottoneCrea, campoCitta);
        configuraValidazioneDataNascita();
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
     * @author Michele Viselli
     */
    @FXML
    private void onCreate(ActionEvent event) throws IOException {
        String nome         = campoNome.getText();
        String cognome      = campoCognome.getText();
        String email     = campoUsername.getText();
        String password     = campoPassword.getText();
        String citta        = campoCitta.getEditor().getText();
        campoDataNascita.commitValue();
        LocalDate dataN = campoDataNascita.getValue();

        // Il ruolo è esclusivo: uno dei due è sempre e solo vero
        boolean isRistoratore = radioRistoratore.isSelected();
        boolean isCliente     = !isRistoratore;

        // Cambi obbligatori
        boolean nomeVuoto     = segnalaSeVuoto(campoNome, erroreNome);
        boolean cognomeVuoto  = segnalaSeVuoto(campoCognome, erroreCognome);
        boolean emailVuota    = segnalaSeVuoto(campoUsername, erroreUsername);
        boolean passwordVuota = segnalaSeVuoto(campoPassword, errorePassword);
        boolean cittaVuota    = segnalaSeVuoto(campoCitta.getEditor(), erroreCitta);

        Date dataNascita;

        if(dataN == null) {
            dataNascita = null;
        } else {
            dataNascita = Date.from(dataN.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        boolean emailNonValida       = !emailVuota && !UtenteDTO.emailValida(email);
        boolean cittaNonValida       = !cittaVuota && !Utility.cittaEsiste(citta);
        boolean formatoDataNonValido = testoDataNascitaNonValido;
        boolean etaNonValida         = !formatoDataNonValido
                && !UtenteDTO.dataNascitaValida(dataNascita);

        if (emailNonValida) erroreUsername.setText("Email non valida");
        if (cittaNonValida) erroreCitta.setText("Città non presente");
        if (formatoDataNonValido) {
            erroreDataNascita.setText("Formato data non valido");
        } else if (etaNonValida) {
            erroreDataNascita.setText("Età minima: 14 anni");
        } else {
            erroreDataNascita.setText("");
        }
        etichettaErrore.setText(cittaNonValida
                ? "Luogo non presente, seleziona una città dall'elenco."
                : "");

        if (nomeVuoto || cognomeVuoto || emailVuota || passwordVuota || cittaVuota
                || emailNonValida || cittaNonValida || formatoDataNonValido || etaNonValida) {
            return;
        }

        String passwordHashed = Utility.calcolaSha256(password);

        UtenteDTO utente = new UtenteDTO(nome,cognome,email,dataNascita, new LuogoDTO(new CittaDTO(citta)), passwordHashed);
        Boolean risposta = (Boolean) GestioneRichieste.getInstance().inviaEAttendi("REG",utente);

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
     * Converte il testo della data senza lasciare propagare gli errori di formato
     * generati dal converter predefinito del DatePicker.
     *
     * @author Michele Viselli
     */
    private void configuraValidazioneDataNascita() {
        LocalDateStringConverter converterPredefinito = new LocalDateStringConverter();

        campoDataNascita.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return converterPredefinito.toString(data);
            }

            @Override
            public LocalDate fromString(String testo) {
                if (testo == null || testo.isBlank()) {
                    testoDataNascitaNonValido = false;
                    erroreDataNascita.setText("");
                    return null;
                }

                try {
                    LocalDate data = converterPredefinito.fromString(testo);
                    testoDataNascitaNonValido = false;
                    erroreDataNascita.setText("");
                    return data;
                } catch (DateTimeParseException e) {
                    testoDataNascitaNonValido = true;
                    erroreDataNascita.setText("Formato data non valido");
                    return campoDataNascita.getValue();
                }
            }
        });

        campoDataNascita.valueProperty().addListener((osservato, precedente, corrente) -> {
            if (corrente == precedente) return;
            testoDataNascitaNonValido = false;
            erroreDataNascita.setText("");
        });
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
