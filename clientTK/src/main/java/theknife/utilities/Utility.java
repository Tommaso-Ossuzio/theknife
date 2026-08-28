package theknife.utilities;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import theknife.model.GestioneRichieste;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utilità condivise fra le schermate.
 * @author Matteo Franguelli
 */
public class Utility {

    private static List<String> cittaDelDatabase;
    private static List<String> cucineDelDatabase;

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
        completamento(campo, cittaDelDatabase, Utility::cittaDalDatabase);
    }

    /**
     * Completa il tipo di cucina mentre si scrive, proponendo le cucine del database.
     * @param campo tendina modificabile in cui si scrive la cucina
     * @author Matteo Franguelli
     */
    public static void completamentoCucina(ComboBox<String> campo) {
        completamento(campo, cucineDelDatabase, Utility::cucineDalDatabase);
    }

    /**
     * Riempie una tendina modificabile e la filtra mentre si scrive.
     * @param campo tendina da completare
     * @param giaPronti elenco gia' in cache, null se va ancora chiesto al server
     * @param caricatore come ottenere l'elenco dal server
     * @author Matteo Franguelli
     */
    private static void completamento(ComboBox<String> campo, List<String> giaPronti, Supplier<List<String>> caricatore) {
        if (campo == null) return;

        ObservableList<String> valori = FXCollections.observableArrayList();
        FilteredList<String> filtrate = new FilteredList<>(valori, nome -> true);
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

        boolean[] inCorso = {false};

        campo.getEditor().textProperty().addListener((osservato, precedente, digitato) -> {
            if (inCorso[0]) return;

            String scelta = campo.getSelectionModel().getSelectedItem();
            if (scelta != null && scelta.equals(digitato)) return;

            Platform.runLater(() -> {
                if (inCorso[0]) return;
                inCorso[0] = true;

                String scritto = campo.getEditor().getText() == null ? "" : campo.getEditor().getText();
                int cursore = campo.getEditor().getCaretPosition();
                String ricerca = scritto.trim().toLowerCase();

                filtrate.setPredicate(nome -> ricerca.isEmpty() || nome.toLowerCase().startsWith(ricerca));

                boolean daMostrare = !ricerca.isEmpty() && !filtrate.isEmpty() && campo.isFocused();
                if (daMostrare && !campo.isShowing()) {
                    campo.show();
                    spazioNonSceglie(campo);
                } else if (!daMostrare && campo.isShowing()) {
                    campo.hide();
                }

                if (!scritto.equals(campo.getEditor().getText())) {
                    campo.getEditor().setText(scritto);
                    campo.getEditor().positionCaret(Math.min(cursore, scritto.length()));
                }

                inCorso[0] = false;
            });
        });

        if (giaPronti != null) {
            valori.setAll(giaPronti);
            return;
        }

        Thread caricamento = new Thread(() -> {
            List<String> disponibili = caricatore.get();
            Platform.runLater(() -> valori.setAll(disponibili));
        }, "elenco-database");
        caricamento.setDaemon(true);
        caricamento.start();
    }

    /**
     * Fa in modo che la barra spaziatrice scriva uno spazio invece di scegliere la voce
     * evidenziata: la lista del popup sta in un'altra scena e interpreta SPACE come Invio.
     * @param campo tendina da correggere
     * @author Matteo Franguelli
     */
    private static void spazioNonSceglie(ComboBox<String> campo) {
        if (!(campo.getSkin() instanceof ComboBoxListViewSkin<?> tendina)) return;

        Node lista = tendina.getPopupContent();
        if (lista == null || lista.getProperties().containsKey("spazio-corretto")) return;

        lista.getProperties().put("spazio-corretto", Boolean.TRUE);

        lista.addEventFilter(KeyEvent.ANY, evento -> {
            if (evento.getCode() != KeyCode.SPACE && !" ".equals(evento.getCharacter())) return;

            if (evento.getEventType() == KeyEvent.KEY_PRESSED) {
                TextField editor = campo.getEditor();
                editor.insertText(editor.getCaretPosition(), " ");
            }
            evento.consume();
        });
    }

    /**
     * Controlla che il luogo digitato sia una delle città del database.
     * Se l'elenco non è ancora arrivato dal server accetta, lasciando la verifica al server.
     * @param scritta nome digitato dall'utente
     * @return true se la città esiste
     * @author Matteo Franguelli
     */
    public static boolean cittaEsiste(String scritta) {
        return esisteNellElenco(cittaDelDatabase, scritta);
    }

    /**
     * Controlla che la cucina digitata sia una di quelle del database.
     * Se l'elenco non è ancora arrivato dal server accetta, lasciando la verifica al server.
     * @param scritta nome digitato dall'utente
     * @return true se la cucina esiste
     * @author Matteo Franguelli
     */
    public static boolean cucinaEsiste(String scritta) {
        return esisteNellElenco(cucineDelDatabase, scritta);
    }

    /**
     * Cerca un nome in un elenco ignorando maiuscole e spazi.
     * @param elenco elenco in cui cercare, null se non è ancora arrivato dal server
     * @param scritta nome digitato dall'utente
     * @return true se il nome esiste, oppure se l'elenco non è ancora disponibile
     * @author Matteo Franguelli
     */
    private static boolean esisteNellElenco(List<String> elenco, String scritta) {
        if (scritta == null || scritta.isBlank()) return false;
        if (elenco == null) return true;

        for (String nome : elenco) {
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

        List<String> ricevute = elencoDalServer("CITTA");
        if (!ricevute.isEmpty()) cittaDelDatabase = ricevute;
        return ricevute;
    }

    /**
     * Chiede al server l'elenco delle cucine del database, una volta sola.
     * @return le cucine del database, vuoto se il server non risponde
     * @author Matteo Franguelli
     */
    private static synchronized List<String> cucineDalDatabase() {
        if (cucineDelDatabase != null) return cucineDelDatabase;

        List<String> ricevute = elencoDalServer("CUC");
        if (!ricevute.isEmpty()) cucineDelDatabase = ricevute;
        return ricevute;
    }

    /**
     * Chiede al server un elenco di nomi.
     * @param comando comando del protocollo da inviare
     * @return i nomi ricevuti, vuoto se il server non risponde
     * @author Matteo Franguelli
     */
    private static List<String> elencoDalServer(String comando) {
        List<String> ricevuti = new ArrayList<>();

        try {
            Object risposta = GestioneRichieste.getInstance().inviaEAttendi(comando);

            if (risposta instanceof List<?> elenco) {
                for (Object nome : elenco) {
                    if (nome instanceof String testo && !testo.isBlank()) {
                        ricevuti.add(testo.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ricevuti;
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
