package theknife.ui.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;
import theknife.utilities.Etichette;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

//TODO da rivedere, vengono usati i file

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

    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE = "users.csv";

    private double latitudine;
    private double longitudine;
    private String googleMapsUrl;

    public void setRestaurantData(String name,
                                  String nation,
                                  String city,
                                  String address,
                                  double latitude,
                                  double longitude,
                                  String price,
                                  String phoneNumber,
                                  boolean delivery,
                                  boolean booking,
                                  String cuisine,
                                  String website,
                                  Integer stelleMichelin) {

        this.latitudine = latitude;
        this.longitudine = longitude;

        etichettaNome.setText(valoreNonNullo(name));
        etichettaIndirizzo.setText(valoreNonNullo(address));

        // Format città
        if (city != null && !city.isBlank()) {
            if (nation != null && !nation.isBlank()) etichettaCitta.setText(city + ", " + nation);
            else etichettaCitta.setText(city);
        } else {
            etichettaCitta.setText(valoreNonNullo(nation));
        }

        String prezzoVisualizzato = Etichette.formattaFasciaPrezzo(price);
        valorePrezzo.setText(prezzoVisualizzato.isBlank() ? "-" : prezzoVisualizzato);
        valoreTelefono.setText((phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber : "-");
        valoreCucina.setText(valoreNonNullo(cuisine));
        valoreConsegna.setText(delivery ? "Disponibile" : "No");
        valorePrenotazione.setText(booking ? "Disponibile" : "No");

        mostraStelleMichelin(stelleMichelin);

        // GESTIONE SITO WEB (WebView)
        if (website != null && !website.isBlank() && !website.equalsIgnoreCase("null")){
            linkSitoWeb.setText(website);
            linkSitoWeb.setDisable(false);
            linkSitoWeb.setOnAction(e -> apriInWebView(website));
            apriInWebView(website);
        } else {
            linkSitoWeb.setText("-");
            linkSitoWeb.setDisable(true);
            mostraMessaggioNessunSito();
        }
        // "Apri in Maps"
        preparaGoogleMapsUrl();
        // Gestione bottone Preferiti in base al ruolo
        aggiornaVisibilitaPreferiti();
    }

    /**
     * Costruisce l'URL per Google Maps utilizzando le coordinate di latitudine e longitudine
     * del ristorante.
     *
     * @author Matteo Franguelli
     */
    private void preparaGoogleMapsUrl() {
        // Il flusso FILTRO non restituisce ancora le coordinate: in quel caso
        // l'URL deve rimanere null, così il pulsante Maps viene disabilitato.
        // TODO: valorizzare nuovamente l'URL quando le coordinate saranno
        // incluse in LuogoDTO dal DAO e restituite dal server.
        String urlFinale = null;

        if (latitudine != 0 && longitudine != 0) {
            // Coordinate precise
            urlFinale = "https://www.google.com/maps?q=" + latitudine + "," + longitudine;
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
    private void onAggiungiAiPreferiti() {
        //Prendo id ristorante
        int idRistorante = 0;
        String nomeR = etichettaNome.getText();
        String indirizzoR = etichettaIndirizzo.getText();

        GestioneRistoranti gr = GestioneRistoranti.getInstance();

        Optional<Ristorante> risto = gr.listaRistoranti.stream().filter(x -> x.getNome().equalsIgnoreCase(nomeR) && x.getLuogo().getIndirizzo().equalsIgnoreCase(indirizzoR)).findFirst();
        if (risto.isPresent())
        {
            idRistorante = risto.get().getId();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("Ristorante non trovato.");
            alert.showAndWait();
            return;
        }

        //Salvo il ristorante nel file user.csv
        if(aggiungiRistoranteSuCSV(idRistorante)) {
            //Confermo L'aggiunta del ristorante
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
     * Aggiunge l'ID del ristorante alla lista dei preferiti dell'utente nel file CSV.
     * Legge il file, individua la riga dell'utente loggato, aggiorna la colonna dei preferiti evitando duplicati, e riscrive il file aggiornato.
     *
     * @param idRistorante
     * @return true se l'operazione è andata a buon fine, false se il ristorante era già presente o se c'è stato un errore I/O.
     * @author Celestino Resteghini
     */
    private boolean aggiungiRistoranteSuCSV(int idRistorante) {
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
    }
}
