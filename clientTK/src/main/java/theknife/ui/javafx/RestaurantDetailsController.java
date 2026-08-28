package theknife.ui.javafx;


import it.uninsubria.dto.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.utilities.Etichette;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Controller della finestra dei ristoranti.
 * @author Matteo FRanguelli
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class RestaurantDetailsController {
    /**
     * Impedisce alle pagine remote di installare cursori basati su immagini.
     * JavaFX WebKit 21 genera una NPE quando il frame di un cursore CSS custom
     * non viene decodificato, invece di ripiegare sul cursore standard.
     */
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

    @FXML
    private void initialize() {
        // Il foglio utente ha precedenza anche sui cursor custom dei siti
        // caricati e lascia WebView interattivo usando soltanto cursori nativi.
        vistaSito.getEngine().setUserStyleSheetLocation(STILE_CURSORE_WEB_SICURO);
    }

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
        String prezzoVisualizzato = Etichette.formattaFasciaPrezzo(ristorante.getFasciaPrezzo());
        valorePrezzo.setText(prezzoVisualizzato.isBlank() ? "-" : prezzoVisualizzato);

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
     * Costruisce l'URL per Google Maps utilizzando le coordinate di latitudine e longitudine
     * del ristorante.
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
     * Gestisce l'evento di click sul pulsante "Apri in Maps".
     * Apre il link generato nel browser predefinito del sistema.
     *@author Matteo Franguelli
     * @author Elia Toschi
     */
    @FXML
    private void onApriMaps() throws IOException {
        preparaGoogleMapsUrl();
        if (googleMapsUrl != null) apriNelBrowser(googleMapsUrl);
    }

    /**
     * Controlla se l'utente corrente ha i permessi da Cliente per visualizzare il pulsante "Aggiungi ai preferiti".
     *
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
     * Gestisce l'azione di aggiunta del ristorante corrente alla lista dei preferiti.
     * Recupera l'ID del ristorante e la lista dei ristoranti e nel caso lo aggiunge nel db
     * Mostra un avviso in caso di successo o errore (ristorante già presente nei preferiti).
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     * @throws IOException
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
     * Chiude la finestra corrente dei dettagli del ristorante.
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onChiudi() {
        Stage st = (Stage) etichettaNome.getScene().getWindow();
        st.close();
    }

    /**
     * Carica un URL specifico all'interno del componente WebView.
     * Aggiunge automaticamente il protocollo http:// se mancante.
     *
     * @param url L'indirizzo web da caricare.
     * @author Matteo Franguelli
     */
    private void apriInWebView(String url) {
        if (vistaSito != null && url != null && !url.isBlank()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            vistaSito.getEngine().load(url);
        }
    }

    /**
     * Visualizza un messaggio HTML se un ristorante non possiede un sito web, in modo da poter mantenere coerente la grafica.
     *
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
     * Apre un URL esterno utilizzando il browser predefinito del sistema operativo
     *
     * @param url
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
     * Restituisce la stringa in input oppure una stringa vuota se l'input è null.
     *
     * @param s
     * @return La stringa originale o "" se null.
     * @author Matteo Franguelli
     */
    private String valoreNonNullo(String s) {
        return s == null ? "" : s;
    }

    /**
     * Visualizza graficamente il numero di stelle Michelin (da 1 a 3)
     * aggiornando il testo e lo stile dell'etichetta.
     *
     * @param stelleMichelin numero di stelle Michelin, oppure {@code null}
     *                       quando il dato non è disponibile
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
     * Metodo per salvare un ristorante preferito nel db
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     * @param idRistorante
     * @throws IOException
     */
    private void aggiungiRistorante(int idRistorante) throws IOException {
       HashMap<String, Integer> id = new HashMap<>();
       id.put("idUtente", idUtente);
       id.put("idRistorante", idRistorante);
       GestioneRichieste.getInstance().inviaSolo("AGG-PREF", id);
    }
}
