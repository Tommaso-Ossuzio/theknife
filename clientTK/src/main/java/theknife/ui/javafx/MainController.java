package theknife.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import theknife.model.GestioneFile;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;
import theknife.model.Luogo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

//TODO da rivedere, vengono usati i file

/**
 * * Controller principale dell'applicazione.
 *  * <p>
 *  * Coordina l'interazione tra vista e logica applicativa,
 *  * gestendo le azioni dell'utente e l'inizializzazione
 *  * dei componenti principali.
 *
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * @author Elia Toschi
 * @author Tommaso Ossuzio
 * @version 2
 */
public class MainController {

    // I ristoranti sono mostrati come card distribuite su più pagine
    @FXML private Pagination paginazione;
    @FXML private VBox statoVuoto;
    @FXML private Label etichettaStatoVuoto;
    @FXML private Label etichettaRisultati;
    @FXML private Label etichettaSelezione;

    @FXML private Button bottoneLogin;
    @FXML private Button bottoneRegistrati;
    @FXML private Button bottoneLogout;
    @FXML private Label etichettaRuolo;
    @FXML private Button bottonePreferiti;
    @FXML private Button bottoneMieRecensioni;
    @FXML private Button bottoneMieiRistoranti;
    @FXML private Button bottoneRispondiRecensioni;

    @FXML private Button bottoneAggiungiRecensione;
    @FXML private Button bottoneAggiungiRistorante;

    @FXML private TextField campoLuogo;
    @FXML private TextField campoCucina;

    // Lista dei ristoranti usata dal codice (dati) collegata alla ListView
    private final ObservableList<Ristorante> ristoranti = FXCollections.observableArrayList();
    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_DATI = "michelin_my_maps.csv";
    GestioneRistoranti gr = GestioneRistoranti.getInstance();

    /** Quante card mostrare in ogni pagina della griglia. */
    private static final int RISTORANTI_PER_PAGINA = 12;

    /** Ristorante attualmente selezionato con un click su una card. */
    private Ristorante ristoranteSelezionato;

    /** Card evidenziata, tenuta da parte per poterla deselezionare. */
    private Node cardSelezionata;

    /**
     * Esegue compiti di inizializzazione:
     * - Caricare i ristoranti dal file
     * - Imposta come la lista deve mostrare i ristoranti
     * - Imposta i pulsanti in base al ruolo (default: Ospite)
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        inizializzaGriglia();

        // Ogni volta che la lista dei ristoranti cambia (caricamento o filtro)
        // la griglia viene ricalcolata e ridivisa in pagine.
        ristoranti.addListener((javafx.collections.ListChangeListener<Ristorante>) c -> aggiornaPaginazione());

        caricaRistorantiDaCsv();
        aggiornaPaginazione();
        aggiornaInterfaccia();

    }

    /**
     * Crea un thread dove crea una lista temporanea dove vengono inseriti tutti i ristoranti,
     * una volta finito vengono caricati nella grafica
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void caricaRistorantiDaCsv() {
        // Avviamo il thread
        new Thread(() -> {
            // Creiamo una lista temporanea per non bloccare la grafica
            List<Ristorante> bufferTemporaneo = new LinkedList<>();
            InputStream is = null;

            try {
                File fileEsterno = new File(NOME_CARTELLA, NOME_FILE_DATI);

                if (fileEsterno.exists()) {
                    System.out.println("Caricamento dati da: " + fileEsterno.getAbsolutePath());
                    is = new FileInputStream(fileEsterno);
                }

                if (is == null) {
                    System.err.println("ERRORE: " + NOME_FILE_DATI + " non trovato.");
                    return;
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String linea = br.readLine();
                    if (linea != null && linea.toLowerCase().contains("name")) {
                        linea = br.readLine();
                    }

                    while (linea != null) {
                        // Passiamo la lista temporanea al metodo
                        aggiungiDaRigaCsv(linea, bufferTemporaneo);
                        linea = br.readLine();
                    }
                }
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Finito il caricamento viene aggiorata la lista Observable
            Platform.runLater(() -> {
                ristoranti.addAll(bufferTemporaneo);
                gr.listaRistoranti.addAll(bufferTemporaneo);
            });

        }).start();
    }


    /**
     * Converte una singola riga CSV in un oggetto Restaurant
     * e lo aggiunge alla lista dei ristoranti.
     * @author Celestino Resteghini
     */
    private void aggiungiDaRigaCsv(String linea, List<Ristorante> destinazione) {
        if (linea == null || linea.isBlank()) return;

        String[] parti = dividiCsv(linea);

        String nome = pulisci(parti[0]);

        String[] s = parti[1].split(",");
        String indirizzo = pulisci(s[0]);;

        s = parti[2].split(",");

        String citta   = s.length > 0 ? pulisci(s[0]) : null;
        String nazione = s.length > 1 ? pulisci(s[1]) : null;


        //TODO: Conversione prezzo
        /*
            € → meno di 35 €
            €€ → tra 35 € e 60 €
            €€€ → tra 60 € e 100 €
            €€€€ → oltre 100 €

            Michelin li descrive anche così:

            € = “per tutte le tasche”
            €€ = “costo ragionevole”
            €€€ = “occasione speciale”
            €€€€ = “piccola follia”
        */
        double prezzo = pulisci(parti[3]).length() * 20; //ogni simbolo = 20€


        LinkedList<String> tipoCucina= new LinkedList<>();
        s = parti[4].split(",");

        // Aggiungi ogni elemento alla LinkedList
        for (String e : s) {
            tipoCucina.add(pulisci(e));
        }

        // Coordinate (attenzione agli errori di formato)
        double latitudine=0;
        double longitudine=0;

        try { longitudine = Double.parseDouble(pulisci(parti[5])); } catch (NumberFormatException ignored) {}

        try { latitudine = Double.parseDouble(pulisci(parti[6])); } catch (NumberFormatException ignored) {}

        String num_tel = pulisci(parti[7]);

        // Link e info aggiuntive
        String link = pulisci(parti[8]);

        String website = pulisci(parti[9]);

        s = parti[10].split(" ");

        double award = -1;
        if(s.length > 1) {
            String a = s[1].substring(0, 4);

            if (parti[10] != null && a.equals("Star")) {
                award = Double.parseDouble(pulisci(s[0]));
            } else {
                award = -1;
            }
        }
        // Se nel CSV non c’è un link, generiamo un link a Google Maps
        if (link == null || link.isBlank()) {
            String maps = "https://www.google.com/maps?q="
                    + inUrl(nome) + "+" + inUrl(indirizzo) + "+" + inUrl(citta);
            link = maps;
        }

        boolean delivery = false;
        boolean booking = false;

        if(parti.length > 14 && "true".equalsIgnoreCase(parti[14]))
            delivery = true;

        if(parti.length > 15 && "true".equalsIgnoreCase(parti[15]))
            booking = true;

        Ristorante r = new Ristorante(nome, num_tel, delivery, booking, prezzo, tipoCucina, new Luogo(nazione, indirizzo, citta, latitudine, longitudine), website, link, award);

        //gr.add(r);

        destinazione.add(r);
    }

    /* =========================================================
       GRIGLIA DI CARD IMPAGINATA
       Le card sono solo presentazione: i dati restano nella
       ObservableList "ristoranti", che non cambia comportamento.
       ========================================================= */

    /**
     * Collega la paginazione alla lista dei ristoranti:
     * ogni pagina costruisce la propria griglia di card su richiesta,
     * così non vengono mai creati migliaia di nodi tutti insieme.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    private void inizializzaGriglia() {
        paginazione.setPageFactory(this::creaPagina);
    }

    /**
     * Ricalcola il numero di pagine dopo un caricamento o un filtro,
     * aggiorna il conteggio dei risultati e mostra lo stato vuoto
     * quando non c'è nulla da elencare.
     * @author Matteo Franguelli
     */
    private void aggiornaPaginazione() {
        int totale = ristoranti.size();
        int pagine = Math.max(1, (int) Math.ceil(totale / (double) RISTORANTI_PER_PAGINA));

        boolean vuoto = (totale == 0);

        paginazione.setPageCount(pagine);
        paginazione.setVisible(!vuoto);
        paginazione.setManaged(!vuoto);

        statoVuoto.setVisible(vuoto);
        statoVuoto.setManaged(vuoto);

        if (etichettaRisultati != null) {
            etichettaRisultati.setText(
                    vuoto ? "" : totale + (totale == 1 ? " ristorante trovato" : " ristoranti trovati")
            );
        }

        // La pagina corrente potrebbe non esistere più dopo un filtro
        if (paginazione.getCurrentPageIndex() >= pagine) {
            paginazione.setCurrentPageIndex(0);
        } else {
            // Forza la ricostruzione della pagina visibile con i nuovi dati
            paginazione.setPageFactory(this::creaPagina);
        }

        deselezionaRistorante();
    }

    /**
     * Costruisce la griglia di card corrispondente a una singola pagina.
     *
     * @param indicePagina pagina richiesta dal controllo di paginazione (parte da 0)
     * @author Matteo Franguelli
     */
    private Node creaPagina(int indicePagina) {
        TilePane griglia = new TilePane();
        griglia.getStyleClass().add("card-grid");

        int da = indicePagina * RISTORANTI_PER_PAGINA;
        int a = Math.min(da + RISTORANTI_PER_PAGINA, ristoranti.size());

        for (int i = da; i < a; i++) {
            griglia.getChildren().add(creaCard(ristoranti.get(i)));
        }

        ScrollPane contenitore = new ScrollPane(griglia);
        contenitore.setFitToWidth(true);
        return contenitore;
    }

    /**
     * Crea la card di un singolo ristorante: nome, indirizzo, prezzo,
     * badge del tipo di cucina e dei servizi, link al sito.
     * Un click seleziona la card, il doppio click apre i dettagli.
     * @author Matteo Franguelli
     */
    private Node creaCard(Ristorante r) {
        VBox card = new VBox();
        card.getStyleClass().add("restaurant-card");

        Label nome = new Label(valoreNonNullo(r.getNome()));
        nome.setWrapText(true);
        nome.getStyleClass().add("restaurant-name");
        card.getChildren().add(nome);

        String indirizzo = (valoreNonNullo(r.getLuogo().getIndirizzo())
                + ", " + valoreNonNullo(r.getLuogo().getCitta())).replaceAll("(^, )|(, $)", "");
        if (!indirizzo.isBlank()) {
            card.getChildren().add(creaRigaCard("📍", indirizzo));
        }

        String prezzo = formattaPrezzo(r.getPrezzo());
        if (!prezzo.isBlank()) {
            card.getChildren().add(creaRigaCard("💰", prezzo));
        }

        String sitoWeb = r.getWebsite();
        if (sitoWeb != null && !sitoWeb.isBlank() && !sitoWeb.equals("null")) {
            Hyperlink link = new Hyperlink(sitoWeb);
            link.getStyleClass().add("restaurant-link");
            link.setMaxWidth(300);
            card.getChildren().add(link);
        }

        // Spinge i badge in fondo alla card, così tutte le card si allineano
        Region spaziatore = new Region();
        VBox.setVgrow(spaziatore, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().add(spaziatore);

        HBox badge = new HBox();
        badge.getStyleClass().add("card-tags");

        String cucina = String.join(", ", r.getCucina());
        if (cucina != null && !cucina.isBlank()) {
            badge.getChildren().add(creaBadge(cucina, "tag"));
        }
        if (r.getAward() > 0) {
            badge.getChildren().add(creaBadge("★ " + (int) r.getAward() + " Michelin", "tag-michelin"));
        }
        if (r.isDelivery()) {
            badge.getChildren().add(creaBadge("Consegna", "tag-accent"));
        }
        if (r.isBooking()) {
            badge.getChildren().add(creaBadge("Prenotabile", "tag-accent"));
        }
        if (!badge.getChildren().isEmpty()) {
            card.getChildren().add(badge);
        }

        card.setOnMouseClicked(e -> {
            selezionaRistorante(r, card);
            if (e.getClickCount() == 2) {
                apriDettagliRistorante(r);
            }
        });

        return card;
    }

    /**
     * Crea una riga "icona + testo" da inserire dentro una card.
     * @author Matteo Franguelli
     */
    private HBox creaRigaCard(String icona, String testo) {
        Label etichettaIcona = new Label(icona);
        etichettaIcona.getStyleClass().add("emoticon");

        Label etichettaTesto = new Label(testo);
        etichettaTesto.setWrapText(true);
        etichettaTesto.getStyleClass().add("card-line-text");

        HBox riga = new HBox(etichettaIcona, etichettaTesto);
        riga.getStyleClass().add("card-line");
        return riga;
    }

    /**
     * Crea un badge colorato (tipo di cucina, stelle Michelin, servizi).
     * @author Matteo Franguelli
     */
    private Label creaBadge(String testo, String classeAggiuntiva) {
        Label etichetta = new Label(testo);
        etichetta.getStyleClass().add("tag");
        if (!"tag".equals(classeAggiuntiva)) {
            etichetta.getStyleClass().add(classeAggiuntiva);
        }
        return etichetta;
    }

    /**
     * Evidenzia la card cliccata e memorizza il ristorante scelto,
     * usato poi dai pulsanti "Aggiungi recensione" e "Visualizza recensioni".
     * @author Matteo Franguelli
     */
    private void selezionaRistorante(Ristorante r, Node card) {
        if (cardSelezionata != null) {
            cardSelezionata.getStyleClass().remove("restaurant-card-selected");
        }
        card.getStyleClass().add("restaurant-card-selected");

        cardSelezionata = card;
        ristoranteSelezionato = r;

        if (etichettaSelezione != null) {
            etichettaSelezione.setText("Selezionato: " + valoreNonNullo(r.getNome()));
        }
    }

    /**
     * Annulla la selezione corrente (dopo un filtro o un cambio pagina).
     * @author Matteo Franguelli
     */
    private void deselezionaRistorante() {
        if (cardSelezionata != null) {
            cardSelezionata.getStyleClass().remove("restaurant-card-selected");
        }
        cardSelezionata = null;
        ristoranteSelezionato = null;

        if (etichettaSelezione != null) {
            etichettaSelezione.setText("Seleziona un ristorante per vedere le azioni disponibili");
        }
    }

    /**
     * Traduce il prezzo medio nella fascia a simboli di euro usata nelle card.
     * @author Matteo Franguelli
     */
    private String formattaPrezzo(double prezzo) {
        if (prezzo <= 0) return "";
        int simboli = Math.max(1, Math.min(4, (int) Math.round(prezzo / 20.0)));
        return "€".repeat(simboli) + "  ·  circa " + (int) prezzo + " €";
    }

    /**
     * Apre una nuova finestra con i dettagli del ristorante selezionato.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void apriDettagliRistorante(Ristorante rd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unininsubria/theknifeui/ui/javafx/view/restaurant_details.fxml"));
            // Il foglio di stile è già dichiarato dentro restaurant_details.fxml
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(valoreNonNullo(rd.getNome()));

            RestaurantDetailsController ctrl = loader.getController();
            ctrl.setRestaurantData(
                    rd.getNome(),
                    rd.getLuogo().getNazione(),
                    rd.getLuogo().getCitta(),
                    rd.getLuogo().getIndirizzo(),
                    rd.getLuogo().getLatitudine(),
                    rd.getLuogo().getLongitudine(),
                    String.valueOf(rd.getPrezzo()),
                    rd.getN_tel(),
                    rd.isDelivery(),
                    rd.isBooking(),
                    String.join(", ", rd.getCucina()),
                    rd.getWebsite(),
                    rd.getAward()
            );
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Aggiorna la visibilità dei pulsanti in base al ruolo dell’utente:
     * - ospite
     * - cliente
     * - ristoratore
     * @author Matteo Franguelli
     */
    private void aggiornaInterfaccia() {
        Session s = Session.getInstance();

        // Recuperiamo i permessi esatti
        boolean isGuest = s.isGuest();
        boolean puoRecensire = s.isCliente();
        boolean puoAggiungereRisto = s.isRistoratore();
        boolean isLogged = !isGuest;

        // Login / Logout / Registrati
        if (bottoneLogin != null) { bottoneLogin.setVisible(isGuest); bottoneLogin.setManaged(isGuest); }
        if (bottoneRegistrati != null) { bottoneRegistrati.setVisible(isGuest); bottoneRegistrati.setManaged(isGuest); }
        if (bottoneLogout != null) { bottoneLogout.setVisible(isLogged); bottoneLogout.setManaged(isLogged); }

        // Etichetta Ruolo in alto
        if (etichettaRuolo != null) {
            if (isGuest) etichettaRuolo.setText("Ospite");
            else if (puoRecensire && puoAggiungereRisto) etichettaRuolo.setText("Cliente e Ristoratore: " + valoreNonNullo(s.getUsername()));
            else if (puoAggiungereRisto) etichettaRuolo.setText("Ristoratore: " + valoreNonNullo(s.getUsername()));
            else etichettaRuolo.setText("Cliente: " + valoreNonNullo(s.getUsername()));
        }

        // Pulsanti Personali
        if (bottonePreferiti != null) {
            bottonePreferiti.setVisible(puoRecensire);
            bottonePreferiti.setManaged(puoRecensire);
        }
        if (bottoneMieRecensioni != null) {
            bottoneMieRecensioni.setVisible(puoRecensire);
            bottoneMieRecensioni.setManaged(puoRecensire);
        }

        if (bottoneMieiRistoranti != null) {
            bottoneMieiRistoranti.setVisible(puoAggiungereRisto);
            bottoneMieiRistoranti.setManaged(puoAggiungereRisto);
        }

        if (bottoneRispondiRecensioni != null) {
            bottoneRispondiRecensioni.setVisible(puoAggiungereRisto);
            bottoneRispondiRecensioni.setManaged(puoAggiungereRisto);
        }

        if (bottoneAggiungiRecensione != null) {
            bottoneAggiungiRecensione.setDisable(!puoRecensire);
        }
        if (bottoneAggiungiRistorante != null) {
            bottoneAggiungiRistorante.setDisable(!puoAggiungereRisto);
        }
    }

    /**
     * Metodo chiamato sia da login che da register, permette l'aggiornamento dell'interfaccia
     * basandosi sulla Session
     * @author Matteo Franguelli
     */
    public void onLoginSuccess() {
        aggiornaInterfaccia();
        Session session = Session.getInstance();
        if (session.isAuthenticated()) {

            String cittaUtente = GestioneFile.recuperaCittaUtente(session.getUsername());
            if (cittaUtente != null && !cittaUtente.isBlank()) {
                session.setCitta(cittaUtente);

                if (campoLuogo != null) {
                    campoLuogo.setText(cittaUtente);
                    onApplyFilters();
                    System.out.println("Filtro applicato automaticamente per città: " + cittaUtente);
                }
            }
        }
    }

    /**
     * Si occupa di mostrare la finestra per effettuare il login.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unininsubria/theknifeui/ui/javafx/view/login.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Login");

            LoginController ctrl = loader.getController();
            ctrl.setParentController(this);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Si occupa di mostrare la finestra di registrazione.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/it/unininsubria/theknifeui/ui/javafx/view/register.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Registrati");

            RegisterController ctrl = loader.getController();
            ctrl.setParentController(this);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Si occupa di disconnettere l'utente nel caso di click
     * sul pulsante logout.
     * @author Matteo Franguelli
     */
    @FXML
    private void onLogout() {
        Session.getInstance().logout();
        aggiornaInterfaccia();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Logout effettuato.");
        alert.showAndWait();

        onResetFilters();

    }

    /**
     * Si occupa di aprire, se permesso, la finestra per aggiungere un ristorante.
     * @author Matteo Franguelli
     */
    @FXML
    private void onAddRestaurant() {
        Session s = Session.getInstance();

        if (!s.isRistoratore()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Permesso negato");
            a.setHeaderText(null);
            a.setContentText("Solo i ristoratori possono aggiungere ristoranti.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/add_restaurant.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(loader.load()));
            st.setTitle("Nuovo ristorante");
            st.initModality(Modality.APPLICATION_MODAL);

            try {
                AddRestaurantController ctrl = loader.getController();
                ctrl.setControllerPrincipale(this);
            } catch (Exception ignored) {}

            st.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }
    /**
     * Si occupa di aprire, se permesso, la finestra per aggiungere una recensione.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @FXML
    private void onAddReview() {
        Session s = Session.getInstance();

        if (!s.isCliente()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Permesso negato");
            a.setHeaderText(null);

            if (s.isGuest())
                a.setContentText("Devi effettuare il login per recensire.");
            else
                a.setContentText("Il tuo account non ha i permessi da Cliente per lasciare recensioni.");

            a.showAndWait();
            return;
        }

        Ristorante selezionato = ristoranteSelezionato;

        if (selezionato == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Nessun ristorante");
            a.setHeaderText(null);
            a.setContentText("Seleziona un ristorante prima di aggiungere una recensione.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/add_review.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(loader.load()));
            st.setTitle("Nuova recensione");
            st.initModality(Modality.APPLICATION_MODAL);

            AddReviewController ctrl = loader.getController();
            ctrl.setRestaurant(selezionato);
            ctrl.setRestaurantName(selezionato.getNome());

            st.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Quando viene premuto il pulsante filtro si occupa dell'applicazione dei parametri
     * di ricerca.
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     */
    @FXML
    protected void onApplyFilters() {
        String luogo = campoLuogo.getText();
        if (luogo == null || luogo.isBlank()) {
            mostraErrore("Campo obbligatorio", "Devi inserire una città per effettuare la ricerca.");
            campoLuogo.requestFocus(); // Rimette il cursore nel campo vuoto
            return;
        }

        LinkedList<Ristorante> rist = gr.Filtro(campoLuogo.getText(), campoCucina.getText(), -1,-1, false, false, -1);

        if(rist!=null)
        {
            ristoranti.clear();       // Svuota lista grafica attuale
            ristoranti.addAll(rist);  // aggiunge risultati filtro
        }
        else { ristoranti.clear(); }
    }

    /**
     * Metodo che permette di inizializzare i metodi di ricerca
     * @author Matteo Franguelli
     */
    @FXML
    public void onResetFilters() {
        if (campoLuogo != null) campoLuogo.clear();
        if (campoCucina != null) campoCucina.clear();

        mostraRistoranti(gr.listaRistoranti);

        System.out.println("[FILTER] Filtri resettati.");
    }

    /**
     * Quando viene premuto il pulsante "I miei ristoranti" mostra i propri ristoranti.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowMyRestaurants() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/my_restaurants.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(loader.load()));
            st.setTitle("I miei ristoranti");
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quando viene premuto il pulsante Preferiti mostra i ristoranti inseriti nei preferiti.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowFavorites() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/favorites.fxml"));
            Scene scene = new Scene(loader.load());
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("I miei preferiti");
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quando viene premuto apre la finestra Filtro Avanzato.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowAdvancedFilter() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/advanced_filter.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(loader.load()));
            st.setTitle("Filtro avanzato");
            st.initModality(Modality.APPLICATION_MODAL);

            AdvancedFilterController ctrl = loader.getController();
            ctrl.setParent(this);

            st.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quando viene premuto apre la finestra che mostra le proprie recensioni.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/my_reviews.fxml"));
            Scene scene = new Scene(loader.load());
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Le mie recensioni");
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quando viene premuto apre la finestra che permette ai ristoratori di rispondere alle recensioni
     * fatte al proprio ristorante
     * @author Matteo Franguelli
     */
    @FXML
    public void onReplyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/reply_review.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Rispondi alle recensioni");
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Permette la visualizzazione di recensioni dopo aver selezionato un ristorante.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @FXML
    private void onViewReviews() {
        Ristorante selezionato = ristoranteSelezionato;

        if (selezionato == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Nessun ristorante");
            a.setHeaderText(null);
            a.setContentText("Seleziona un ristorante prima di vedere le recensioni.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unininsubria/theknifeui/ui/javafx/view/view_reviews.fxml")); // Controlla che il path sia giusto
            Stage st = new Stage();
            st.setScene(new Scene(loader.load()));
            st.setTitle("Recensioni");
            st.initModality(Modality.APPLICATION_MODAL);

            ViewReviewsController ctrl = loader.getController();
            ctrl.setRestaurant(selezionato);

            st.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }


    /**
     * Metodo generico per mostrare errori.
     * @param titolo
     * @param messaggio
     * @author Matteo Franguelli
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    /**
     * Rimuove eventuali doppi apici e spazi inutili.
     * @author Matteo Franguelli
     */
    private String pulisci(String s) {
        if (s == null) return "";
        return s.replace("\"", "").trim();
    }

    /**
     * Restituisce la stringa se non è null, altrimenti stringa vuota.
     * Utile per evitare NullPointerException nelle concatenazioni.
     * @author Matteo Franguelli
     */
    private String valoreNonNullo(String s) {
        return s == null ? "" : s;
    }

    /**
     * Converte uno spazio in '+' per poter usare la stringa in una URL.
     * @author Matteo Franguelli
     */
    private String inUrl(String s) {
        return s == null ? "" : s.trim().replace(" ", "+");
    }

    /**
     * Divide una riga CSV in campi, gestendo i campi tra doppi apici.
     * @author Matteo Franguelli
     */
    private String[] dividiCsv(String line) {
        // split che gestisce anche i campi tra doppi apici
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * Aggiunge la lista di ristoranti alla grafica
     * @param nuovaLista
     * @author Matteo Franguelli
     */
    public void mostraRistoranti(List<Ristorante> nuovaLista) {
        ristoranti.clear();
        if (nuovaLista != null && !nuovaLista.isEmpty()) {
            ristoranti.addAll(nuovaLista);
        }
    }
};