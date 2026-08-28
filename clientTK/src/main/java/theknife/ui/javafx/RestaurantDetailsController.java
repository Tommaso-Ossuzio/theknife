package theknife.ui.javafx;


import it.uninsubria.dto.CoordinateDTO;
import it.uninsubria.dto.RistoranteDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;
import theknife.utilities.Etichette;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

//TODO da rivedere, vengono usati i file

/**
 * Controller della finestra dei ristoranti.
 * @author Matteo FRanguelli
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class RestaurantDetailsController {
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

    //todo da cancellare private static final String NOME_CARTELLA = "data";
    //todo da cancellare private static final String NOME_FILE = "users.csv";

    private double latitudineLoc;
    private double longitudineLoc;
    private String googleMapsUrl;
    private  RistoranteDTO ristoranteDTO;
    LinkedList<RistoranteDTO> preferiti;
    int idUtente;

    public void setRestaurantData(RistoranteDTO ristorante) throws IOException {
        this.ristoranteDTO = ristorante;
        // 1. Estrazione sicura degli oggetti annidati per evitare NullPointerException
        it.uninsubria.dto.LuogoDTO luogo = ristorante.getLuogo();
        it.uninsubria.dto.CittaDTO citta = (luogo != null) ? luogo.getCitta() : null;
        it.uninsubria.dto.CoordinateDTO coord = (luogo != null) ? luogo.getCoordinate() : null;
        // 2. Assegnazione sicura coordinate
        this.latitudineLoc = (coord != null) ? coord.getLatitudine() : 0.0;
        this.longitudineLoc = (coord != null) ? coord.getLongitudine() : 0.0;
        // 3. Assegnazione testi base
        etichettaNome.setText(valoreNonNullo(ristorante.getNome()));

        String via = (luogo != null) ? luogo.getVia() : null;
        etichettaIndirizzo.setText(valoreNonNullo(via));
        // 4. Formattazione Città in totale sicurezza
        if (citta != null) {
            // Presumo che citta.getNome() ti dia il nome in stringa, altrimenti puoi lasciare citta.toString()
            String nomeCitta = citta.getNome();
            String nazione = citta.getNazione();

            if (nazione != null && !nazione.isBlank()) {
                etichettaCitta.setText(nomeCitta + ", " + nazione);
            } else {
                etichettaCitta.setText(valoreNonNullo(nomeCitta));
            }
        } else {
            etichettaCitta.setText("-"); // Se non c'è la città, stampiamo un trattino
        }
        // 5. Prezzo, Telefono, Delivery, Prenotazione
        String prezzoVisualizzato = Etichette.formattaFasciaPrezzo(ristorante.getFasciaPrezzo());
        valorePrezzo.setText(prezzoVisualizzato.isBlank() ? "-" : prezzoVisualizzato);

        valoreTelefono.setText((ristorante.getTelefono() != null && !ristorante.getTelefono().isBlank()) ? ristorante.getTelefono() : "-");

        valoreConsegna.setText(ristorante.isDelivery() ? "Disponibile" : "No");
        valorePrenotazione.setText(ristorante.isPrenotazioneOnline() ? "Disponibile" : "No");
        // 6. Formattazione pulita per le Cucine (evita le parentesi quadre di Java)
        if (ristorante.getCucine() != null && !ristorante.getCucine().isEmpty()) {
            valoreCucina.setText(String.join(", ", ristorante.getCucine()));
        } else {
            valoreCucina.setText("-");
        }
        // 7. Stelle Michelin
        mostraStelleMichelin(ristorante.getStelleMichelin());
        // 8. Gestione Sito Web (WebView)
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
        // 9. Link Esterni e Preferiti
        //todo per me non va bene qua vanno preparate solo quando si clicca su apri in maps no?
        preparaGoogleMapsUrl();
        aggiornaVisibilitaPreferiti();
    }

    /**
     * Costruisce l'URL per Google Maps utilizzando le coordinate di latitudine e longitudine
     * del ristorante.
     *
     * @author Matteo Franguelli
     */
    private void preparaGoogleMapsUrl() throws IOException {
        // Il flusso FILTRO non restituisce ancora le coordinate: in quel caso
        // l'URL deve rimanere null, così il pulsante Maps viene disabilitato.
        // TODO: valorizzare nuovamente l'URL quando le coordinate saranno
        // incluse in LuogoDTO dal DAO e restituite dal server.
        String urlFinale = null;

        Integer id= ristoranteDTO.getIdRistorante();

        CoordinateDTO coordinate =(CoordinateDTO) GestioneRichieste.getInstance().inviaEAttendi("MAPS",id);
        latitudineLoc=coordinate.getLatitudine();
        longitudineLoc=coordinate.getLongitudine();
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
     *
     * @author Matteo Franguelli
     */
    @FXML
    private void onApriMaps() {
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
     * Recupera l'ID del ristorante e invoca il salvataggio su file CSV.
     * Mostra un avviso in caso di successo o errore (ristorante non trovato).
     *
     * @author Celestino Resteghini
     */
    @FXML
    private void onAggiungiAiPreferiti() throws IOException {
        //todo da cancellare  GestioneRistoranti gr = GestioneRistoranti.getInstance();

        //todo da cancellare Optional<Ristorante> risto = gr.listaRistoranti.stream().filter(x -> x.getNome().equalsIgnoreCase(nomeR) && x.getLuogo().getIndirizzo().equalsIgnoreCase(indirizzoR)).findFirst();

        Session session = Session.getInstance();
        idUtente = session.getID();
        preferiti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("VIS-PREF", idUtente);

        /*if (preferiti.contains(ristoranteDTO)) {
            idRistorante = ristoranteDTO.getIdRistorante();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("Ristorante non trovato.");
            alert.showAndWait();
            return;
        }todo da cancellare */

        //se il ristorante è già nei preferiti
        if(preferiti.contains(ristoranteDTO)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("Ristorante già nei preferiti.");
            alert.showAndWait();
        } else {
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

    //todo Da cancellare
    /**
     * Aggiunge l'ID del ristorante alla lista dei preferiti dell'utente nel file CSV.
     * Legge il file, individua la riga dell'utente loggato, aggiorna la colonna dei preferiti evitando duplicati, e riscrive il file aggiornato.
     *
     * @param idRistorante
     * @return true se l'operazione è andata a buon fine, false se il ristorante era già presente o se c'è stato un errore I/O.
     * @author Celestino Resteghini
     */
    /*private boolean aggiungiRistoranteSuCSV(int idRistorante) {
        String usernameU = Session.getInstance().getUsername();
        File fileUtenti = new File(NOME_CARTELLA, NOME_FILE);
        if (!fileUtenti.exists()) return false;
        List<String> righe = new LinkedList<>();
        String primaparte="";
        String mieiRist="";
        String idRistorantiPres="";


        try (BufferedReader lettore = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String linea;

            while ((linea = lettore.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] parti = linea.split(";");

                // Formato CSV atteso: username;hash;nome;cognome;città;isCliente;isRistoratore;RistorantiPreferiti;MieiRistoranti
                if (parti.length >= 2) {
                    if (parti[0].equals(usernameU)) {

                        //Salvo la prima parte della riga
                        primaparte = parti[0]+";"+parti[1]+";"+parti[2]+";"+parti[3]+";"+parti[4]+";"+parti[5]+";"+parti[6]+";"+parti[7]+";";

                        // Nella colonna Ristoranti preferiti ci sarà il seguente formato: "1-4-5"
                        if (parti.length > 8 && !parti[8].isEmpty()) {
                            idRistorantiPres = parti[8].trim();
                            String[] s1 = parti[8].split("-");
                            //Controllo se il ristorante era già presente nei preferiti
                            for(String stringa: s1)
                                if (idRistorante == Integer.valueOf(stringa)) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Attenzione");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Ristorante già nei preferiti.");
                                    alert.showAndWait();
                                    return false;
                                }

                            //Salvo il nuovo ristorante
                            idRistorantiPres = idRistorantiPres.trim()+"-"+String.valueOf(idRistorante);
                            if(parti.length>9)
                                mieiRist=parti[9];
                            continue; // SALTA QUESTA RIGA (è quella vecchia)
                        }
                        else
                        {
                            idRistorantiPres = String.valueOf(idRistorante);
                            if(parti.length>9)
                                mieiRist=parti[9];
                            continue; // SALTA QUESTA RIGA (è quella vecchia)
                        }
                    }
                }
                righe.add(linea); // Tieni tutte le altre
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Aggiungi la NUOVA versione in fondo alla lista
        String nuovaRiga = primaparte + idRistorantiPres +";"+mieiRist;
        righe.add(nuovaRiga);

        // Riscrivi il file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileUtenti, StandardCharsets.UTF_8))) {
            for (String r : righe) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {}
        return true;
    }*/

    private void aggiungiRistorante(int idRistorante) throws IOException {
       HashMap<String, Integer> id = new HashMap<>();
       id.put("idUtente", idUtente);
       id.put("idRistorante", idRistorante);
       GestioneRichieste.getInstance().inviaSolo("AGG-PREF", idUtente);
    }
}
