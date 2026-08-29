package it.uninsubria;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller della finestra di avvio del server.
 * @author Celestino Resteghini
 */
public class ServerController {

    @FXML private TextField campoHost;
    @FXML private TextField campoPorta;
    @FXML private TextField campoUtente;
    @FXML private PasswordField campoPassword;
    @FXML private Label etichettaErrore;
    @FXML private Label etichettaStato;
    @FXML private VBox riquadroForm;
    @FXML private VBox riquadroLog;
    @FXML private TextArea areaLog;
    @FXML private Button bottoneAvvia;

    /**
     * Verifica le credenziali indicate e, se sono valide, avvia il server.
     * @author Celestino Resteghini
     */
    @FXML
    private void onAvvia() {
        String host = campoHost.getText().trim();
        String porta = campoPorta.getText().trim();
        String utente = campoUtente.getText().trim();
        String password = campoPassword.getText();

        if (host.isEmpty() || porta.isEmpty() || utente.isEmpty()) {
            etichettaErrore.setText("Host, porta e utente sono obbligatori.");
            return;
        }
        if (!porta.matches("[0-9]+")) {
            etichettaErrore.setText("La porta deve essere un numero.");
            return;
        }

        etichettaErrore.setText("");
        bottoneAvvia.setDisable(true);
        DatabaseConfig.configura(host, porta, utente, password);

        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(DatabaseConfig.getDefaultUrl(), utente, password)) {
                conn.getCatalog();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    etichettaErrore.setText("Connessione fallita: " + e.getMessage());
                    bottoneAvvia.setDisable(false);
                });
                return;
            }
            Platform.runLater(this::mostraLog);
            try {
                DatabaseConfig.inizializzaDatabaseCompleto();
                System.out.println("Server in ascolto sulla porta 8999");
                AppServer.exec();
            } catch (IOException e) {
                System.out.println("Errore del server: " + e.getMessage());
            }
        }).start();
    }

    private void mostraLog() {
        etichettaStato.setText("Server avviato su " + DatabaseConfig.getTargetUrl());
        riquadroForm.setVisible(false);
        riquadroForm.setManaged(false);
        riquadroLog.setVisible(true);
        riquadroLog.setManaged(true);
        bottoneAvvia.setText("Arresta server");
        bottoneAvvia.setDisable(false);
        bottoneAvvia.setOnAction(e -> System.exit(0));
        System.setOut(flussoVerso(System.out));
        System.setErr(flussoVerso(System.err));
    }

    private PrintStream flussoVerso(PrintStream originale) {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                originale.write(b, off, len);
                String testo = new String(b, off, len, StandardCharsets.UTF_8);
                Platform.runLater(() -> areaLog.appendText(testo));
            }
        }, true, StandardCharsets.UTF_8);
    }
}
