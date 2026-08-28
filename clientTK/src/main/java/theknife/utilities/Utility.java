package theknife.utilities;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import theknife.model.GestioneRichieste;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilità condivise fra le schermate.
 * @author Matteo Franguelli
 */
public class Utility {

    private static List<String> cittaDelDatabase;

    /**
     * Permette di confermare con il tasto Invio, oltre che con il pulsante.
     * @param conferma pulsante di conferma della schermata
     * @param campi campi in cui Invio deve valere come conferma
     * @author Matteo Franguelli
     */
    public static void confermaConInvio(Button conferma, Node... campi) {
        if (conferma == null) return;

        conferma.setDefaultButton(true);

        for (Node campo : campi) {
            if (campo == null) continue;

            campo.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
                if (evento.getCode() != KeyCode.ENTER || conferma.isDisabled()) return;
                if (campo instanceof ComboBox<?> tendina && tendina.isShowing()) return;

                conferma.fire();
                evento.consume();
            });
        }
    }

    /**
     * Completa il nome della città mentre si scrive, proponendo le città del database.
     * @param campo tendina modificabile in cui si scrive la città
     * @author Matteo Franguelli
     */
    public static void completamentoCitta(ComboBox<String> campo) {
        if (campo == null) return;

        ObservableList<String> citta = FXCollections.observableArrayList();
        FilteredList<String> filtrate = new FilteredList<>(citta, nome -> true);
        campo.setItems(filtrate);

        campo.setCellFactory(lista -> {
            ListCell<String> cella = new ListCell<>() {
                @Override
                protected void updateItem(String nome, boolean vuota) {
                    super.updateItem(nome, vuota);
                    setText(vuota ? null : nome);
                }
            };
            cella.setPrefWidth(0);
            return cella;
        });

        campo.getEditor().textProperty().addListener((osservato, precedente, digitato) -> {
            String scelta = campo.getSelectionModel().getSelectedItem();
            if (scelta != null && scelta.equals(digitato)) return;

            String ricerca = digitato == null ? "" : digitato.trim().toLowerCase();
            filtrate.setPredicate(nome -> ricerca.isEmpty() || nome.toLowerCase().startsWith(ricerca));

            if (!ricerca.isEmpty() && !filtrate.isEmpty() && campo.isFocused()) {
                campo.show();
            } else {
                campo.hide();
            }
        });

        if (cittaDelDatabase != null) {
            citta.setAll(cittaDelDatabase);
            return;
        }

        Thread caricamento = new Thread(() -> {
            List<String> disponibili = cittaDalDatabase();
            Platform.runLater(() -> citta.setAll(disponibili));
        }, "citta-database");
        caricamento.setDaemon(true);
        caricamento.start();
    }

    /**
     * Controlla che il luogo digitato sia una delle città del database.
     * Se l'elenco non è ancora arrivato dal server accetta, lasciando la verifica al server.
     * @param scritta nome digitato dall'utente
     * @return true se la città esiste
     * @author Matteo Franguelli
     */
    public static boolean cittaEsiste(String scritta) {
        if (scritta == null || scritta.isBlank()) return false;
        if (cittaDelDatabase == null) return true;

        for (String nome : cittaDelDatabase) {
            if (nome.equalsIgnoreCase(scritta.trim())) return true;
        }
        return false;
    }

    /**
     * Chiede al server l'elenco delle città del database.
     * @return le città del database, vuoto se il server non risponde
     * @author Matteo Franguelli
     */
    private static synchronized List<String> cittaDalDatabase() {
        if (cittaDelDatabase != null) return cittaDelDatabase;

        List<String> ricevute = new ArrayList<>();

        try {
            Object risposta = GestioneRichieste.getInstance().inviaEAttendi("CITTA");

            if (risposta instanceof List<?> elenco) {
                for (Object nome : elenco) {
                    if (nome instanceof String testo && !testo.isBlank()) {
                        ricevute.add(testo.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!ricevute.isEmpty()) cittaDelDatabase = ricevute;
        return ricevute;
    }

    /**
    * Calcola l'hash SHA-256 di una stringa.
    *
    * @author Matteo Franguelli
    */
    public static String calcolaSha256(String input) {
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
}
