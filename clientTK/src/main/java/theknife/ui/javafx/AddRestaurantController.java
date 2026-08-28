package theknife.ui.javafx;

import it.uninsubria.dto.RistoranteDTO;
import it.uninsubria.dto.RistoratoreDTO;
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

    /**
     * Permette di salvare il ristorante anche premendo Invio.
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        Utility.confermaConInvio(bottoneSalva);
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
        boolean latitudineErrata  = segnalaSeNonNumerico(campoLatitudine, erroreLatitudine);
        boolean longitudineErrata = segnalaSeNonNumerico(campoLongitudine, erroreLongitudine);
        boolean prezzoErrato      = segnalaSeNonNumerico(campoPrezzo, errorePrezzo);
        boolean cucinaErrata      = segnalaSeFormatoCucinaErrato(campoTipoCucina, erroreTipoCucina);

        if (nomeVuoto || nazioneVuota || cittaVuota || indirizzoVuoto || latitudineVuota
                || longitudineVuota || telefonoVuoto || prezzoVuoto || cucinaVuota
                || latitudineErrata || longitudineErrata || prezzoErrato || cucinaErrata) {
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
        int stelle=0; //TODO da mettere il risultato ottenuto dalla nuovo textBox che va ancora fatta
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
     * Scrive "Numero non valido" di fianco al campo se non contiene un numero.
     * @param campo campo numerico da controllare
     * @param errore etichetta accanto al campo
     * @return true se il valore non è un numero
     * @author Matteo Franguelli
     */
    private boolean segnalaSeNonNumerico(TextField campo, Label errore) {
        try {
            Double.parseDouble(campo.getText().trim());
            errore.setText("");
            return false;
        } catch (NumberFormatException e) {
            errore.setText("Numero non valido");
            return true;
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