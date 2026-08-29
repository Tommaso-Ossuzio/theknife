package theknife.ui.javafx;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RistoranteDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import theknife.model.*;
import theknife.utilities.Utility;

import java.io.*;

/**
 * Classe che si occupa della gestione di nuove Recensioni.
 * @author Elia Toschi
 * @author Matteo Franguelli
 * @author Celestino Resteghini
 * @author Michele Viselli
 */
public class AddReviewController {
    private boolean modalitaModifica = false;
    private MyReviewsController.ReviewRow recensioneOriginale;

    @FXML private Label etichettaTitolo;
    @FXML private TextArea areaRecensione;
    @FXML private Label etichettaErrore;

    // Riferimenti ai 5 bottoni stella
    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Button bottonePubblica;

    private RistoranteDTO ristoranteDTODestinazione;

    // Variabile per tenere traccia del voto (default 5 stelle)
    private int votoSelezionato = 5;

    /**
     * Seleziona il ristorante
     * @author Michele Viselli
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    public void setRestaurant() {
        this.ristoranteDTODestinazione = null;
    }

    /**
     * Seleziona il ristorante ricevuto dal nuovo flusso server.
     *
     * @param restaurant ristorante DTO da recensire
     *
     * @author Michele Viselli
     */
    public void setRestaurant(RistoranteDTO restaurant) {
        this.ristoranteDTODestinazione = restaurant;
    }

    /**
     * Seleziona il ristorante dal nome
     * @author Matteo Franguelli
     * @param nomeRistorante
     */
    public void setRestaurantName(String nomeRistorante) {
        if (etichettaTitolo != null && nomeRistorante != null && !nomeRistorante.isBlank()) {
            etichettaTitolo.setText("Recensisci: " + nomeRistorante);
        }
    }

    /**
     * Inizializza le stelle impostate a 5
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        // Appena si apre la finestra, coloriamo le stelle in base al default (5)
        aggiornaGraficaStelle();
        Utility.confermaConInvio(bottonePubblica);
    }

    /**
     * Gestisce l'azione click delle stelle
     * @author Matteo Franguelli
     * @param event
     */
    @FXML
    private void onStarClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        // Leggiamo "1", "2"... dallo userData definito nell'FXML
        String val = (String) btn.getUserData();
        votoSelezionato = Integer.parseInt(val);

        aggiornaGraficaStelle();
    }

    /**
     * Modifica il numero di stelle selezionate nella grafica
     * @author Matteo Franguelli
     */
    private void aggiornaGraficaStelle() {
        Button[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            stars[i].getStyleClass().remove("star-button-on");
            if (i < votoSelezionato) {
                stars[i].getStyleClass().add("star-button-on");
            }
        }
    }

    /**
     * gestisce l'operazione di annullamento
     * @author Matteo Franguelli
     */
    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    /**
     * Chiude la finestra
     * @author Matteo Franguelli
     */
    private void chiudiFinestra() {
        Stage finestra = (Stage) areaRecensione.getScene().getWindow();
        finestra.close();
    }

    /**
     * Aggiunge e scrive la recensione su file
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @author Matteo Franguelli
     * @author Michele Viselli
     */
    @FXML
    private void onCreate() throws IOException {
        Session sessione = Session.getInstance();

        String testo = areaRecensione.getText();
        int numeroStelle = votoSelezionato;
        int idUtente = sessione.getID();
        int idRistorante = ristoranteDTODestinazione.getIdRistorante();

        RecensioneDTO recensioneDTO = new RecensioneDTO(testo, numeroStelle, idUtente, idRistorante);
        GestioneRichieste.getInstance().inviaSolo("AGG-REC", recensioneDTO);

        //TODO fare il refresh della pagina sottostante
        // Al momento funziona, ma dopo aver aggiunto la recensione non compare subito a livello grafico nel ristorante

        chiudiFinestra();
    }

    /**
     * Metodo usato per modificare una recensione.
     * @author Matteo Franguelli
     */
    public void setDatiPerModifica(MyReviewsController.ReviewRow recensioneVecchia) {
        this.modalitaModifica = true;
        this.recensioneOriginale = recensioneVecchia;

        // Riempi i campi con i dati vecchi
        areaRecensione.setText(recensioneVecchia.getText());
        votoSelezionato = recensioneVecchia.getRating();
        aggiornaGraficaStelle();

        // Cambia titolo
        if (etichettaTitolo != null) etichettaTitolo.setText("Modifica recensione");
    }
}
