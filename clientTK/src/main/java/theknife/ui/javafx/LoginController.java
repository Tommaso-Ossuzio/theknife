/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;

import it.uninsubria.dto.AuthDTO;

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
import java.util.HashMap;

/**
 * Controller della finestra di accesso: verifica le credenziali sul server e
 * imposta la sessione con i permessi corrispondenti.
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
     * Indica la schermata da avvisare quando l'accesso va a buon fine.
     * @param parentController schermata che ha aperto la finestra di accesso
     * @author Matteo Franguelli
     */
    public void setParentController(ControllerAutenticazione parentController) {
        this.controllerPrincipale = parentController;
    }

    /**
     * Controlla i campi, tenta l'accesso e segnala le credenziali non valide.
     * @author Matteo Franguelli
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
     * @author Matteo Franguelli
     */
    @FXML
    private void onBack(ActionEvent event) {
        chiudiFinestra();
    }

    /**
     * Chiede al server di verificare le credenziali e, se corrette, apre la sessione.
     * @param email email inserita dall'utente
     * @param password password in chiaro, cifrata prima dell'invio
     * @return true se le credenziali sono valide
     * @author Matteo Franguelli
     * @author Celestino Resteghini
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
     * Chiude la finestra di accesso.
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        ((Stage) campoUsername.getScene().getWindow()).close();
    }
}