/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;


import it.uninsubria.dto.*;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Controller della scheda di un ristorante, con i suoi dati e il suo sito web.
 * @author Matteo Franguelli
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class RestaurantDetailsController {
    /** Foglio di stile che impedisce ai siti aperti di cambiare il cursore del mouse. */
    private static final String STILE_CURSORE_WEB_SICURO =
            "data:text/css;charset=utf-8;base64,KiB7IGN1cnNvcjogYXV0byAhaW1wb3J0YW50OyB9";

    @FXML private Label etichettaNome;
    @FXML private Label etichettaIndirizzo;
    @FXML private Label etichettaCitta;
    @FXML private Label valorePrezzo;
    @FXML private Label valoreCucina;
    @FXML private Label valoreConsegna;
    @FXML private Label valorePrenotazione;
    @FXML private Label valoreTelefono;
    @FXML private Label valoreStelle;

    @FXML private Hyperlink linkSitoWeb;

    @FXML private WebView vistaSito;

    @FXML private Button bottoneApriMaps;
    @FXML private Button bottonePreferiti;

    private double latitudineLoc;
    private double longitudineLoc;
    private String googleMapsUrl;
    private  RistoranteDTO ristoranteDTO;
    LinkedList<RistoranteDTO> preferiti;
    int idUtente;

    /**
     * Prepara la WebView in cui viene mostrato il sito del ristorante.
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        // Il foglio utente ha precedenza anche sui cursor custom dei siti
        // caricati e lascia WebView interattivo usando soltanto cursori nativi.
        vistaSito.getEngine().setUserStyleSheetLocation(STILE_CURSORE_WEB_SICURO);
        vistaSito.getEngine().getLoadWorker().stateProperty().addListener(
                (osservato, precedente, corrente) -> {
                    if (corrente == Worker.State.FAILED) {
                        Platform.runLater(this::mostraMessaggioSitoNonDisponibile);
                    }
                });
    }

    /**
     * Riempie la scheda con i dati del ristorante e ne carica il sito.
     * @param ristorante ristorante da mostrare
     * @author Matteo Franguelli
     * @author Elia Toschi
     */
    public void setRestaurantData(RistoranteDTO ristorante) throws IOException {
        this.ristoranteDTO = ristorante;
        LuogoDTO luogo = ristorante.getLuogo();
        CittaDTO citta = (luogo != null) ? luogo.getCitta() : null;
        CoordinateDTO coord = (luogo != null) ? luogo.getCoordinate() : null;
        this.latitudineLoc = (coord != null) ? coord.getLatitudine() : 0.0;
        this.longitudineLoc = (coord != null) ? coord.getLongitudine() : 0.0;
        etichettaNome.setText(valoreNonNullo(ristorante.getNome()));

        String via = (luogo != null) ? luogo.getVia() : null;
        etichettaIndirizzo.setText(valoreNonNullo(via));
        if (citta != null) {
            String nomeCitta = citta.getNome();
            String nazione = citta.getNazione();

            if (nazione != null && !nazione.isBlank()) {
                etichettaCitta.setText(nomeCitta + ", " + nazione);
            } else {
                etichettaCitta.setText(valoreNonNullo(nomeCitta));
            }
        } else {
            etichettaCitta.setText("-");
        }
        String fasciaPrezzo = valoreNonNullo(ristorante.getFasciaPrezzo());
        valorePrezzo.setText(fasciaPrezzo.isBlank() ? "-" : fasciaPrezzo);

        valoreTelefono.setText((ristorante.getTelefono() != null && !ristorante.getTelefono().isBlank()) ? ristorante.getTelefono() : "-");

        valoreConsegna.setText(ristorante.isDelivery() ? "Disponibile" : "No");
        valorePrenotazione.setText(ristorante.isPrenotazioneOnline() ? "Disponibile" : "No");
        if (ristorante.getCucine() != null && !ristorante.getCucine().isEmpty()) {
            valoreCucina.setText(String.join(", ", ristorante.getCucine()));
        } else {
            valoreCucina.setText("-");
        }
        mostraStelleMichelin(ristorante.getStelleMichelin());
        if (ristorante.getSitoWeb() != null && !ristorante.getSitoWeb().isBlank() && !ristorante.getSitoWeb().equalsIgnoreCase("null")) {
            linkSitoWeb.setText(ristorante.getSitoWeb());
            linkSitoWeb.setDisable(false);
            linkSitoWeb.setOnAction(e -> apriInWebView(ristorante.getSitoWeb()));
            apriInWebView(ristorante.getSitoWeb());
        } else {
            linkSitoWeb.setText("-");
            linkSitoWeb.setDisable(true);
            mostraMessaggioNessunSito();
        }

        aggiornaVisibilitaPreferiti();
    }

    /**
     * Chiede al server le coordinate del ristorante e prepara il link a Google Maps.
     * @author Matteo Franguelli
     * @author Elia Toschi
     */
    private void preparaGoogleMapsUrl() throws IOException {
               String urlFinale = null;

        Integer id= ristoranteDTO.getIdRistorante();

        CoordinateDTO coordinate =(CoordinateDTO) GestioneRichieste.getInstance().inviaEAttendi("MAPS",id);
        if (coordinate != null) {
            latitudineLoc=coordinate.getLatitudine();
            longitudineLoc=coordinate.getLongitudine();
        }
        if (latitudineLoc != 0 && longitudineLoc != 0) {
            // Coordinate precise
           urlFinale = "https://www.google.com/maps?q=" + latitudineLoc + "," + longitudineLoc;
        }

        this.googleMapsUrl = urlFinale;

        if (bottoneApriMaps != null) {
            boolean disponibile = (googleMapsUrl != null);
            bottoneApriMaps.setDisable(!disponibile);
            if (!disponibile) bottoneApriMaps.setText("Maps non disponibile");
            else bottoneApriMaps.setText("Apri in Maps");
        }
    }

    /**
     * Apre la posizione del ristorante nel browser predefinito.
     * @author Matteo Franguelli
     * @author Elia Toschi
     */
    @FXML
    private void onApriMaps() throws IOException {
        preparaGoogleMapsUrl();
        if (googleMapsUrl != null) apriNelBrowser(googleMapsUrl);
    }

    /**
     * Mostra il pulsante dei preferiti solo a chi ha i permessi da Cliente.
     * @author Matteo Franguelli
     */
    private void aggiornaVisibilitaPreferiti() {
        Session s = Session.getInstance();
        boolean visibile = s.isCliente();
        if (bottonePreferiti != null) {
            bottonePreferiti.setVisible(visibile);
            bottonePreferiti.setManaged(visibile);
        }
    }

    /**
     * Salva il ristorante fra i preferiti, avvisando se c'era già.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    @FXML
    private void onAggiungiAiPreferiti() throws IOException {
        Session session = Session.getInstance();
        idUtente = session.getID();
        preferiti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("VIS-PREF", idUtente);

        boolean presente = false;

        for (RistoranteDTO r : preferiti) {
            if(r.getIdRistorante() == ristoranteDTO.getIdRistorante()) {
                presente = true;
                break;
            }
        }

        //se il ristorante è già nei preferiti
        if(presente){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("Ristorante già nei preferiti.");
            alert.showAndWait();
        } else {
            preferiti.add(ristoranteDTO);
            aggiungiRistorante(ristoranteDTO.getIdRistorante());
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Preferiti");
            a.setHeaderText(null);
            a.setContentText("Ristorante aggiunto ai preferiti.");
            a.showAndWait();
        }
    }

    /**
     * Chiude la scheda del ristorante.
     * @author Matteo Franguelli
     */
    @FXML
    private void onChiudi() {
        Stage st = (Stage) etichettaNome.getScene().getWindow();
        st.close();
    }

    /**
     * Carica un sito nella WebView, aggiungendo http:// se manca.
     * @param url indirizzo del sito
     * @author Matteo Franguelli
     */
    private void apriInWebView(String url) {
        if (vistaSito != null && url != null && !url.isBlank()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            try {
                vistaSito.getEngine().load(url);
            } catch (IllegalArgumentException e) {
                mostraMessaggioSitoNonDisponibile();
            }
        }
    }

    /**
     * Mostra un messaggio al posto del sito quando il ristorante non ne ha uno.
     * @author Matteo Franguelli
     */
    private void mostraMessaggioNessunSito() {
        if (vistaSito != null) {
            String html = """
                    <html>
                      <body>
                        <h2>Nessun sito web disponibile</h2>
                        <p>Questo ristorante non ha un sito web specificato.</p>
                      </body>
                    </html>
                    """;
            vistaSito.getEngine().loadContent(html);
        }
    }

    /**
     * Mostra un messaggio quando il sito del ristorante non si riesce a caricare.
     * @author Michele Viselli
     */
    private void mostraMessaggioSitoNonDisponibile() {
        if (vistaSito != null) {
            String html = """
                    <html>
                      <body>
                        <h2>Sito web non disponibile</h2>
                        <p>Non è stato possibile caricare il sito del ristorante.</p>
                      </body>
                    </html>
                    """;
            vistaSito.getEngine().loadContent(html);
        }
    }

    /**
     * Apre un indirizzo nel browser predefinito del sistema.
     * @param url indirizzo da aprire
     * @author Matteo Franguelli
     */
    private void apriNelBrowser(String url) {
        if (url == null || url.isBlank()) return;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce la stringa, oppure una stringa vuota se è null.
     * @param s stringa da controllare
     * @return la stringa originale, "" se era null
     * @author Matteo Franguelli
     */
    private String valoreNonNullo(String s) {
        return s == null ? "" : s;
    }

    /**
     * Mostra le stelle Michelin, da 1 a 3, oppure che il ristorante non ne ha.
     * @param stelleMichelin numero di stelle, null se il dato non è disponibile
     * @author Matteo Franguelli
     */
    private void mostraStelleMichelin(Integer stelleMichelin) {
        valoreStelle.getStyleClass().remove("michelin-none");

        if (stelleMichelin == null) {
            valoreStelle.setText("Dato non disponibile");
            valoreStelle.getStyleClass().add("michelin-none");
            return;
        }

        if (stelleMichelin <= 0) {
            valoreStelle.setText("Nessuna stella Michelin");
            valoreStelle.getStyleClass().add("michelin-none");
            return;
        }
        long stellePiene = Math.min(3, stelleMichelin);

        StringBuilder sb = new StringBuilder();

        // Costruisce la stringa di stelle
        for (int i = 0; i < 3; i++) {
            if (i < stellePiene) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }

        valoreStelle.setText(sb.toString());
    }

    /**
     * Chiede al server di salvare il ristorante fra i preferiti dell'utente.
     * @param idRistorante ristorante da salvare
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void aggiungiRistorante(int idRistorante) throws IOException {
       HashMap<String, Integer> id = new HashMap<>();
       id.put("idUtente", idUtente);
       id.put("idRistorante", idRistorante);
       GestioneRichieste.getInstance().inviaSolo("AGG-PREF", id);
    }
}
