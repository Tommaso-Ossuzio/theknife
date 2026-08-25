package theknife.ui.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import theknife.utilities.Utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO da rivedere, vengono usati i file

/**
 * Controller della finestra di login.
 * Gestisce l'accesso leggendo i permessi specifici dal CSV data/users.csv.
 * @author Matteo Franguelli
 */
public class LoginController {

    @FXML private TextField campoUsername;
    @FXML private PasswordField campoPassword;
    @FXML private Label etichettaErrore;
    @FXML private Button bottoneAccedi;

    private ControllerAutenticazione controllerPrincipale;

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE = "users.csv";

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
     * @param event
     * @author Matteo Franguelli
     */
    @FXML
    private void onLogin(ActionEvent event) {
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
     * Cerca l'utente nel CSV e, se presente, imposta la sessione con i permessi corretti.
     * @author Matteo Franguelli
     */
    private boolean eseguiLogin(String nomeUtente, String password) {
        File fileUtenti = new File(NOME_CARTELLA, NOME_FILE);
        if (!fileUtenti.exists()) return false;

        try (BufferedReader lettore = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String hashPassword = calcolaSha256(password);
            String linea;

            while ((linea = lettore.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] parti = linea.split(";");

                // Formato CSV atteso: username;hash;nome;cognome;città;isCliente;isRistoratore
                if (parti.length >= 2) {
                    if (parti[0].equals(nomeUtente) && parti[1].equals(hashPassword)) {

                        boolean isCliente = true;
                        boolean isRistoratore = false;

                        if (parti.length >= 7) {
                            isCliente = Boolean.parseBoolean(parti[5]);
                            isRistoratore = Boolean.parseBoolean(parti[6]);
                        }
                        else if (parti.length >= 6) {
                            isRistoratore = Boolean.parseBoolean(parti[5]);
                        }
                        Session.Role ruoloMain = isRistoratore ? Session.Role.RISTORATORE : Session.Role.CLIENTE;
                        Session.getInstance().login(nomeUtente, ruoloMain);
                        Session.getInstance().setPermessi(isCliente, isRistoratore);
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
     * Permette di criptare la password per essere memorizzata.
     * @param testo
     * @return
     * @author Matteo Franguelli
     */
    private String calcolaSha256(String testo) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(testo.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    /**
     * Chiude la finestra di Login.
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        ((Stage) campoUsername.getScene().getWindow()).close();
    }
}