package theknife.ui.javafx;

import it.uninsubria.dto.AuthDTO;
import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.UtenteDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.utilities.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Controller della finestra di login.
 * Gestisce l'accesso richiedendo i permessi specifici al DB
 * @author Matteo Franguelli
 * @author Celestino Resteghini
 */
public class LoginController {

    @FXML private TextField campoUsername;
    @FXML private PasswordField campoPassword;
    @FXML private Label etichettaErrore;
    @FXML private Button bottoneAccedi;

    private ControllerAutenticazione controllerPrincipale;

    /**
     * Permette di accedere anche premendo Invio.
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        Utility.confermaConInvio(bottoneAccedi);
    }

    /**
     * Esclude la possibilità di agire a comandi su altre finestre
     * oltre a quella corrente.
     * @param parentController
     * @author Matteo Franguelli
     */
    public void setParentController(ControllerAutenticazione parentController) {
        this.controllerPrincipale = parentController;
    }

    /**
     * Metodo che si occupa della finestra di Login.
     * @author Matteo Franguelli
     * @param event
     * @throws IOException
     */
    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        String nomeUtente = campoUsername.getText();
        String password   = campoPassword.getText();

        if (nomeUtente == null || nomeUtente.isBlank() || password == null || password.isBlank()) {
            etichettaErrore.setText("Compila email e password.");
            return;
        }

        if (eseguiLogin(nomeUtente, password)) {
            if (controllerPrincipale != null) {
                controllerPrincipale.onLoginSuccess();
            }
            chiudiFinestra();
        } else {
            etichettaErrore.setText("Credenziali non valide.");
        }
    }

    /**
     * Chiude la finestra senza accedere.
     * @param event
     * @author Matteo Franguelli
     */
    @FXML
    private void onBack(ActionEvent event) {
        chiudiFinestra();
    }

    /**
     * Cerca l'utente nel DB e, se presente, imposta la sessione con i permessi corretti.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     * @param email
     * @param password
     * @return true se la ricerca è andata a buon fine
     * @throws IOException
     */
    private boolean eseguiLogin(String email, String password) throws IOException {
        AuthDTO credenziali = new AuthDTO(email, Utility.calcolaSha256(password));
        HashMap<String, Boolean> hm = (HashMap<String, Boolean>) GestioneRichieste.getInstance().inviaEAttendi("LOG",credenziali);
        Boolean risposta = hm.get("LOG");
        Boolean is_ristoratore = hm.get("is_ristoratore");

        if(risposta) {
            Session.Role ruoloMain = is_ristoratore ? Session.Role.RISTORATORE : Session.Role.CLIENTE;
            Session.getInstance().login(email, ruoloMain);
            Session.getInstance().setPermessi(!is_ristoratore, is_ristoratore); //.setPermessi(isCliente, isRistoratore);
        }

        return risposta;
    }

    /**
     * Chiude la finestra di Login.
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        ((Stage) campoUsername.getScene().getWindow()).close();
    }
}