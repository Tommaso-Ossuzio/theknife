package theknife.ui.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.GestioneRecensioni;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;

import java.util.LinkedList;

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

    @FXML private CheckBox checkDelivery;
    @FXML private CheckBox checkBooking;

    private int stelleSelezionate = 0;

    // Fasce di prezzo della guida Michelin, con i limiti in euro di ciascuna
    private static final String[] FASCE_PREZZO = {
            "Meno di 35 €",
            "Tra 35 € e 60 €",
            "Tra 60 € e 100 €",
            "Oltre 100 €"
    };
    private static final double[] PREZZO_MIN = { -1, 35, 60, 100 };
    private static final double[] PREZZO_MAX = { 35, 60, 100, -1 };

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
            // L'aspetto è definito in style.css: .star-button / .star-button-on
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
        String luogo = campoLuogo.getText();
        String cucina = campoCucina.getText();
        boolean delivery = checkDelivery.isSelected();
        boolean booking = checkBooking.isSelected();

        if (luogo == null || luogo.isBlank()) {
            mostraErrore("Campo obbligatorio", "Devi inserire una città per effettuare la ricerca.");
            campoLuogo.requestFocus();
            return;
        }

        // Nessuna fascia scelta: -1 vale come "senza limite" per Filtro
        int fascia = menuPrezzo.getSelectionModel().getSelectedIndex();
        double prezzoMin = fascia < 0 ? -1 : PREZZO_MIN[fascia];
        double prezzoMax = fascia < 0 ? -1 : PREZZO_MAX[fascia];

        if (controllerPrincipale != null) {
            GestioneRistoranti gr = GestioneRistoranti.getInstance();
            LinkedList<Ristorante> rist = gr.Filtro(luogo, cucina, prezzoMin, prezzoMax, delivery, booking, stelleSelezionate);

            controllerPrincipale.mostraRistoranti(rist);
        }

        chiudiFinestra();
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