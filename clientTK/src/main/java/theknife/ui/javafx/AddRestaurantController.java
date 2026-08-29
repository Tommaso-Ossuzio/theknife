package theknife.ui.javafx;

import it.uninsubria.dto.RistoranteDTO;
import it.uninsubria.dto.RistoratoreDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.utilities.Utility;

import java.io.*;
import java.util.List;


/**
 * Controller per la gestione dell'aggiunta di nuovi ristoranti.
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 */
public class AddRestaurantController {

    // Campi di testo per inserire i dati del ristorante
    @FXML private TextField campoNome;
    @FXML private TextField campoNazione;
    @FXML private TextField campoCitta;
    @FXML private TextField campoIndirizzo;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoLatitudine;
    @FXML private TextField campoLongitudine;
    @FXML private TextField campoPrezzo;
    @FXML private TextField campoTipoCucina;
    @FXML private CheckBox checkConsegna;
    @FXML private CheckBox checkPrenotazione;
    @FXML private TextField campoSitoWeb;

    // Etichetta per mostrare i messaggi di errore all’utente
    @FXML private Label etichettaErrore;
    @FXML private Label erroreNome;
    @FXML private Label erroreNazione;
    @FXML private Label erroreCitta;
    @FXML private Label erroreIndirizzo;
    @FXML private Label erroreLatitudine;
    @FXML private Label erroreLongitudine;
    @FXML private Label erroreTelefono;
    @FXML private Label errorePrezzo;
    @FXML private Label erroreTipoCucina;
    @FXML private Button bottoneSalva;
    @FXML private Button stella1, stella2, stella3;

    private int stelleSelezionate = 0;

    /**
     * Permette di salvare il ristorante anche premendo Invio.
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        Utility.confermaConInvio(bottoneSalva);
        campoTelefono.setTextFormatter(new TextFormatter<>(modifica ->
                modifica.getControlNewText().matches("\\+?\\d*") ? modifica : null));
    }

    /**
     * Metodo chiamato quando l’utente preme il pulsante "Salva ristorante".
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @FXML
    private void onSalva() throws IOException {
        boolean nomeVuoto        = segnalaSeVuoto(campoNome, erroreNome);
        boolean nazioneVuota     = segnalaSeVuoto(campoNazione, erroreNazione);
        boolean cittaVuota       = segnalaSeVuoto(campoCitta, erroreCitta);
        boolean indirizzoVuoto   = segnalaSeVuoto(campoIndirizzo, erroreIndirizzo);
        boolean latitudineVuota  = segnalaSeVuoto(campoLatitudine, erroreLatitudine);
        boolean longitudineVuota = segnalaSeVuoto(campoLongitudine, erroreLongitudine);
        boolean telefonoVuoto    = segnalaSeVuoto(campoTelefono, erroreTelefono);
        boolean prezzoVuoto      = segnalaSeVuoto(campoPrezzo, errorePrezzo);
        boolean cucinaVuota      = segnalaSeVuoto(campoTipoCucina, erroreTipoCucina);
        boolean latitudineErrata  = segnalaSeCoordinataErrata(
                campoLatitudine, erroreLatitudine, -90, 90, "Latitudine");
        boolean longitudineErrata = segnalaSeCoordinataErrata(
                campoLongitudine, erroreLongitudine, -180, 180, "Longitudine");
        boolean telefonoErrato    = segnalaSeTelefonoErrato(campoTelefono, erroreTelefono);
        boolean prezzoErrato      = segnalaSePrezzoErrato(campoPrezzo, errorePrezzo);
        boolean cucinaErrata      = segnalaSeFormatoCucinaErrato(campoTipoCucina, erroreTipoCucina);

        if (nomeVuoto || nazioneVuota || cittaVuota || indirizzoVuoto || latitudineVuota
                || longitudineVuota || telefonoVuoto || prezzoVuoto || cucinaVuota
                || latitudineErrata || longitudineErrata || telefonoErrato
                || prezzoErrato || cucinaErrata) {
            return;
        }

        String nome = campoNome.getText();
        String nazione = campoNazione.getText();
        String citta = campoCitta.getText();
        String indirizzo = campoIndirizzo.getText();
        double lat = 0;
        if(!campoLatitudine.getText().isEmpty())
            lat = Double.valueOf(campoLatitudine.getText());
        double longi = 0;
        if(!campoLongitudine.getText().isEmpty())
            longi = Double.valueOf(campoLongitudine.getText());
        String numTel = campoTelefono.getText();
        double prezzo = 0;
        if(!campoPrezzo.getText().isEmpty())
            prezzo = Double.valueOf(campoPrezzo.getText());
        String tipo = campoTipoCucina.getText();
        List<String> cucine = java.util.Arrays.stream(tipo.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        String sito = campoSitoWeb.getText();
        boolean delivery = checkConsegna.isSelected();
        boolean booking = checkPrenotazione.isSelected();
        int stelle = stelleSelezionate;
        RistoratoreDTO ristoratore = new RistoratoreDTO(Session.getInstance().getID());

        RistoranteDTO nuovoRistorante = new RistoranteDTO(nome, nazione, citta, indirizzo, lat, longi, numTel, prezzo, cucine, sito, delivery, booking, stelle, ristoratore);
        GestioneRichieste.getInstance().inviaSolo("AGG-RIST",nuovoRistorante);

        // Avviso l'utente del successo
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Salvataggio effettuato");
        a.setHeaderText(null);
        a.setContentText("Ristorante salvato correttamente.");
        a.showAndWait();

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
     * Controlla che una coordinata sia un numero finito compreso nel suo
     * intervallo geografico. Se il campo e' vuoto conserva "Obbligatorio".
     * @param campo campo contenente la coordinata
     * @param errore etichetta accanto al campo
     * @param minimo valore minimo ammesso
     * @param massimo valore massimo ammesso
     * @param nome nome della coordinata mostrato nel messaggio
     * @return true se la coordinata non e' valida
     * @author Michele Viselli
     */
    private boolean segnalaSeCoordinataErrata(TextField campo, Label errore,
                                               double minimo, double massimo, String nome) {
        String testo = campo.getText();
        if (testo == null || testo.isBlank()) return false;

        try {
            double coordinata = Double.parseDouble(testo.trim());
            if (!Double.isFinite(coordinata)) {
                errore.setText("Numero non valido");
                return true;
            }
            if (coordinata < minimo || coordinata > massimo) {
                errore.setText(nome + " tra " + (int) minimo + " e " + (int) massimo);
                return true;
            }

            errore.setText("");
            return false;
        } catch (NumberFormatException e) {
            errore.setText("Numero non valido");
            return true;
        }
    }

    /**
     * Controlla che il prezzo sia un numero finito e non negativo.
     * Se il campo e' vuoto conserva l'errore "Obbligatorio" gia' impostato.
     * @param campo campo contenente il prezzo medio
     * @param errore etichetta accanto al campo
     * @return true se il prezzo non e' valido
     * @author Michele Viselli
     */
    private boolean segnalaSePrezzoErrato(TextField campo, Label errore) {
        String testo = campo.getText();
        if (testo == null || testo.isBlank()) return false;

        try {
            double prezzo = Double.parseDouble(testo.trim());
            if (!Double.isFinite(prezzo)) {
                errore.setText("Numero non valido");
                return true;
            }
            if (prezzo < 0) {
                errore.setText("Il prezzo non può essere negativo");
                return true;
            }

            errore.setText("");
            return false;
        } catch (NumberFormatException e) {
            errore.setText("Numero non valido");
            return true;
        }
    }

    /**
     * Controlla che il telefono contenga soltanto cifre e, facoltativamente,
     * un unico segno + all'inizio.
     * @param campo campo contenente il numero di telefono
     * @param errore etichetta accanto al campo
     * @return true se il telefono non e' valido
     * @author Michele Viselli
     */
    private boolean segnalaSeTelefonoErrato(TextField campo, Label errore) {
        String testo = campo.getText();
        if (testo == null || testo.isBlank()) return false;

        boolean nonValido = !testo.matches("\\+?\\d+");
        errore.setText(nonValido ? "Telefono non valido" : "");
        return nonValido;
    }

    /**
     * Imposta le stelle Michelin sul valore della stella premuta.
     * @param event evento della stella premuta
     * @author Matteo Franguelli
     */
    @FXML
    private void onStellaMichelinClicked(ActionEvent event) {
        Button premuta = (Button) event.getSource();
        stelleSelezionate = Integer.parseInt((String) premuta.getUserData());
        aggiornaGraficaStelle();
    }

    /**
     * Riporta le stelle Michelin a zero.
     * @author Matteo Franguelli
     */
    @FXML
    private void onAzzeraStelleMichelin() {
        stelleSelezionate = 0;
        aggiornaGraficaStelle();
    }

    /**
     * Colora le stelle fino al valore selezionato.
     * @author Matteo Franguelli
     */
    private void aggiornaGraficaStelle() {
        Button[] stelle = {stella1, stella2, stella3};
        for (int i = 0; i < stelle.length; i++) {
            stelle[i].getStyleClass().remove("star-button-on");
            if (i < stelleSelezionate) {
                stelle[i].getStyleClass().add("star-button-on");
            }
        }
    }

    /**
     * Metodo chiamato quando l’utente preme il pulsante "Annulla".
     * Non salva niente, semplicemente chiude la finestra.
     * @author Matteo Franguelli
     */
    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    /**
     * Chiude la finestra corrente.
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage finestra = (Stage) campoNome.getScene().getWindow();
        finestra.close();
    }

    /**
     * Verifica che la stringa delle cucine utilizzi unicamente la virgola come separatore.
     * @param campo
     * @param errore
     * @return true se il formato è errato, false se è corretto
     * @author Celestino Resteghini
     */
    private boolean segnalaSeFormatoCucinaErrato(TextField campo, Label errore) {
        String testo = campo.getText().trim();

        if (testo.matches(".*[;.\\-\\/].*")) {
            errore.setText("Usa solo la virgola come separatore");
            return true;
        }

        if (!testo.contains(",") && testo.contains(" ")) {
            errore.setText("Separa le cucine con una virgola (es. Italiana, Cinese)");
            return true;
        }

        return false;
    }
}
