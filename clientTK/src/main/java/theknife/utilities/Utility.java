package theknife.utilities;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.FiltroRistoranteDTO;
import it.uninsubria.dto.RistoranteDTO;
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
import theknife.model.GestioneRistoranti;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utilità condivise fra le schermate.
 * @author Matteo Franguelli
 */
public class Utility {

    private static Map<String, Integer> cittaDelDatabase;

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
                    if (vuota || nome == null) {
                        setText(null);
                        return;
                    }
                    int quanti = cittaDelDatabase == null ? 0 : cittaDelDatabase.getOrDefault(nome, 0);
                    setText(nome + "   ·   " + quanti + (quanti == 1 ? " ristorante" : " ristoranti"));
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

        Thread caricamento = new Thread(() -> {
            Map<String, Integer> disponibili = cittaDalDatabase();
            Platform.runLater(() -> citta.setAll(disponibili.keySet()));
        }, "citta-database");
        caricamento.setDaemon(true);
        caricamento.start();
    }

    /**
     * Chiede al server tutti i ristoranti e conta quanti ne ha ogni città.
     * @return le città del database, con quanti ristoranti contengono
     * @author Matteo Franguelli
     */
    private static synchronized Map<String, Integer> cittaDalDatabase() {
        //TODO Fare un comando ad hoc per ottenere i nomi di tutte le città
        if (cittaDelDatabase != null) return cittaDelDatabase;

        Map<String, Integer> conteggi = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        FiltroRistoranteDTO tutti = new FiltroRistoranteDTO(null, null, null, null, null, null);

        for (RistoranteDTO ristorante : GestioneRistoranti.getInstance().filtraRistoranti(tutti)) {
            CittaDTO citta = ristorante.getLuogo() == null ? null : ristorante.getLuogo().getCitta();
            if (citta != null && citta.getNome() != null && !citta.getNome().isBlank()) {
                conteggi.merge(citta.getNome().trim(), 1, Integer::sum);
            }
        }

        cittaDelDatabase = conteggi;
        return cittaDelDatabase;
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
