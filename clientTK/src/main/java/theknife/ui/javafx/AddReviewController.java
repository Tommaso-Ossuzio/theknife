/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
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
        String testo = areaRecensione.getText() != null ? areaRecensione.getText().trim() : "";
        if (testo.isBlank()) {
            if (etichettaErrore != null) {
                etichettaErrore.setText("Inserisci un testo per la recensione.");
            }
            return;
        }

        Session sessione = Session.getInstance();
        int idUtente = sessione.getID();
        int numeroStelle = votoSelezionato;

        if (modalitaModifica) {
            int idRecensione = recensioneOriginale.getRawRecensioneId();
            RecensioneDTO recensioneDTO = new RecensioneDTO(idRecensione, testo, numeroStelle, idUtente);
            GestioneRichieste.getInstance().inviaSolo("MOD-REC", recensioneDTO);

            if (ristoranteDTODestinazione != null) {
                int vecchiVoto = recensioneOriginale.getRating();
                int nuovoVoto = numeroStelle;
                int delta = nuovoVoto - vecchiVoto;

                int numRec = ristoranteDTODestinazione.getNumeroRecensioni() == null ? 1 : ristoranteDTODestinazione.getNumeroRecensioni();
                double mediaVecchia = ristoranteDTODestinazione.getMediaStelle() == null ? 0.0 : ristoranteDTODestinazione.getMediaStelle();

                double nuovaMedia = ((mediaVecchia * numRec) + delta) / numRec;

                ristoranteDTODestinazione.setMediaStelle(nuovaMedia);
            }

        } else {
            //crea nuova recensione
            int idRistorante = ristoranteDTODestinazione.getIdRistorante();

            RecensioneDTO recensioneDTO = new RecensioneDTO(testo, numeroStelle, idUtente, idRistorante);
            GestioneRichieste.getInstance().inviaSolo("AGG-REC", recensioneDTO);

            int numeroRecensioniAttuale = ristoranteDTODestinazione.getNumeroRecensioni() == null
                    ? 0
                    : ristoranteDTODestinazione.getNumeroRecensioni();
            double mediaAttuale = ristoranteDTODestinazione.getMediaStelle() == null
                    ? 0.0
                    : ristoranteDTODestinazione.getMediaStelle();
            int nuovoNumeroRecensioni = numeroRecensioniAttuale + 1;
            double nuovaMedia = (mediaAttuale * numeroRecensioniAttuale + numeroStelle)
                    / nuovoNumeroRecensioni;

            ristoranteDTODestinazione.setNumeroRecensioni(nuovoNumeroRecensioni);
            ristoranteDTODestinazione.setMediaStelle(nuovaMedia);

            if (ristoranteDTODestinazione == null) {
                if (etichettaErrore != null) {
                    etichettaErrore.setText("Nessun ristorante selezionato.");
                }
                return;
            }
        }

        chiudiFinestra();
    }

    /**
     * Metodo usato per modificare una recensione.
     * @author Matteo Franguelli
     */
    public void setDatiPerModifica(MyReviewsController.ReviewRow recensioneVecchia) {
        this.modalitaModifica = true;
        this.recensioneOriginale = recensioneVecchia;

        areaRecensione.setText(recensioneVecchia.getText());
        votoSelezionato = recensioneVecchia.getRating();
        aggiornaGraficaStelle();

        if (etichettaTitolo != null) etichettaTitolo.setText("Modifica recensione");
    }
}
