package theknife.ui.javafx;

import it.uninsubria.dto.FiltroRistoranteDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.utilities.Utility;

//TODO da rivedere

/**
 * Classe che si occupa della gestione del Filtro Avanzato.
 * @author Celestino Resteghini
 */
public class AdvancedFilterController {

    @FXML private TextField campoLuogo;
    @FXML private TextField campoCucina;
    @FXML private ComboBox<String> menuPrezzo;

    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Button bottoneApplica;

    @FXML private CheckBox checkDelivery;
    @FXML private CheckBox checkBooking;

    private int stelleSelezionate = 0;

    private static final String[] FASCE_PREZZO = {
            "meno di 35 €",
            "tra 35 € e 60 €",
            "tra 60 € e 100 €",
            "oltre 100 €"
    };

    private MainController controllerPrincipale;

    public void setParent(MainController parent) {
        this.controllerPrincipale = parent;
    }


    /**
     * Riempie il menu con le quattro fasce di prezzo della guida Michelin.
     * @author Celestino Resteghini
     */
    @FXML
    private void initialize() {
        menuPrezzo.getItems().setAll(FASCE_PREZZO);
        Utility.confermaConInvio(bottoneApplica);
    }

    /**
     * Si occupa di aggiornare le stelle in base a quante sono cliccate.
     * @author Celestino Resteghini
     * @param event
     */
    @FXML
    private void onStarClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String val = (String) btn.getUserData();
        stelleSelezionate = Integer.parseInt(val);
        aggiornaGraficaStelle();
    }

    /**
     * resetta le stelle selezionate
     * @author Matteo Franguelli
     */
    @FXML
    private void onResetStars() {
        stelleSelezionate = 0;
        aggiornaGraficaStelle();
    }

    /**
     * Permette di aggiornare le stelle visualizzabili in base a quelle selezionate.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void aggiornaGraficaStelle() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].getStyleClass().remove("star-button-on");
            if (i < stelleSelezionate) {
                stars[i].getStyleClass().add("star-button-on");
            }
        }
    }

    /**
     * Effettua il controllo del modulo di Filtro Avanzato
     * per verificare se rispetta i parametri.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @FXML
    private void onApply() {
        String luogo = normalizza(campoLuogo.getText());
        String cucina = normalizza(campoCucina.getText());

        if (luogo == null || luogo.isBlank()) {
            mostraErrore("Campo obbligatorio", "Devi inserire una città per effettuare la ricerca.");
            campoLuogo.requestFocus();
            return;
        }

        String fasciaPrezzo = menuPrezzo.getValue();
        Double mediaStelle = stelleSelezionate == 0
                ? null
                : (double) stelleSelezionate;
        Boolean delivery = checkDelivery.isSelected() ? Boolean.TRUE : null;
        Boolean prenotazione = checkBooking.isSelected() ? Boolean.TRUE : null;

        FiltroRistoranteDTO filtro = new FiltroRistoranteDTO(
                luogo,
                cucina,
                fasciaPrezzo,
                mediaStelle,
                delivery,
                prenotazione);

        if (controllerPrincipale != null) {
            controllerPrincipale.applicaFiltro(filtro);
        }

        chiudiFinestra();
    }

    /**
     * Normalizza i valori di luogo e cucina
     * @param valore
     * @return
     *
     * @author Michele Viselli
     */
    private String normalizza(String valore) {
        if (valore == null) return null;
        String normalizzato = valore.trim();
        return normalizzato.isEmpty() ? null : normalizzato;
    }

    /**
     * Si occupa di mostrare l'errore nel caso di inserimento.
     * @param titolo
     * @param messaggio
     * @author Matteo Franguelli
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore nell'inserimento");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    /**
     * Richiama il metodo per chiudere la finestra.
     * @author Matteo Franguelli
     */
    @FXML
    private void onCancel() {
        chiudiFinestra();
    }

    /**
     * Chiude la finestra Filtro Avanzato.
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage stage = (Stage) campoLuogo.getScene().getWindow();
        stage.close();
    }
}
