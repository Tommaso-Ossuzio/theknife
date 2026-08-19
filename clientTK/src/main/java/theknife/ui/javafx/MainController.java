package theknife.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import theknife.model.GestioneFile;
import theknife.model.GestioneRistoranti;
import theknife.model.Ristorante;

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
public class MainController implements ControllerAutenticazione {

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

    @FXML private Button bottoneAggiungiRecensione;
    @FXML private Button bottoneTema;

    @FXML private TextField campoLuogo;
    @FXML private TextField campoCucina;

    // Lista dei ristoranti usata dal codice (dati) collegata alla ListView
    private final ObservableList<Ristorante> ristoranti = FXCollections.observableArrayList();
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

        caricaCatalogo();
        aggiornaPaginazione();
        aggiornaInterfaccia();
        aggiornaPulsanteTema();

    }

    /**
     * Imposta la città da cui parte la ricerca e applica subito il filtro.
     * Viene chiamato dalla schermata di benvenuto con il luogo indicato
     * dall'ospite o con la città dell'utente appena entrato.
     *
     * @param citta città con cui pre-compilare la ricerca
     * @author Matteo Franguelli
     */
    public void impostaLuogoIniziale(String citta) {
        if (citta == null || citta.isBlank() || campoLuogo == null) return;

        campoLuogo.setText(citta);
        onApplyFilters();
    }

    /**
     * Cambia il tema di tutta l'applicazione.
     * @author Matteo Franguelli
     */
    @FXML
    private void onCambiaTema() {
        Temi.alterna();
        aggiornaPulsanteTema();
    }

    /**
     * Allinea simbolo e descrizione del pulsante al tema in uso.
     * @author Matteo Franguelli
     */
    private void aggiornaPulsanteTema() {
        if (bottoneTema == null) return;

        bottoneTema.setText(Temi.simboloPulsante());
        bottoneTema.setTooltip(new Tooltip(Temi.descrizionePulsante()));
        // Nome letto dagli strumenti di accessibilità, che sul solo simbolo
        // non saprebbero dire a cosa serve il pulsante
        bottoneTema.setAccessibleText(Temi.descrizionePulsante());
    }

    /**
     * Mostra il catalogo dei ristoranti.
     * <p>
     * Di norma il catalogo è già stato letto dalla schermata di benvenuto,
     * che ne ha bisogno per proporre l'elenco delle città: in quel caso qui
     * basta mostrarlo. Se invece non fosse ancora disponibile, viene letto
     * ora in un thread separato per non bloccare la grafica, e la lista viene
     * riempita al termine sul thread applicativo.
     *
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void caricaCatalogo() {
        if (gr.isCaricato()) {
            mostraRistoranti(gr.listaRistoranti);
            return;
        }

        new Thread(() -> {
            gr.caricaDaCsv();
            Platform.runLater(() -> mostraRistoranti(gr.listaRistoranti));
        }).start();
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

        // I badge vanno a capo da soli: in una sola riga i testi più lunghi
        // (media, cucina, Michelin) verrebbero troncati dalla larghezza della card
        FlowPane badge = new FlowPane();
        badge.getStyleClass().add("card-tags");

        // La media delle recensioni è l'informazione più cercata: apre la riga dei badge
        badge.getChildren().add(Etichette.creaBadgeMedia(r));

        String cucina = String.join(", ", r.getCucina());
        if (cucina != null && !cucina.isBlank()) {
            badge.getChildren().add(Etichette.creaBadge(cucina, "tag"));
        }

        Label michelin = Etichette.creaBadgeMichelin(r);
        if (michelin != null) {
            badge.getChildren().add(michelin);
        }
        if (r.isDelivery()) {
            badge.getChildren().add(Etichette.creaBadge("Consegna", "tag-accent"));
        }
        if (r.isBooking()) {
            badge.getChildren().add(Etichette.creaBadge("Prenotabile", "tag-accent"));
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
        // Il foglio di stile è già dichiarato dentro restaurant_details.fxml
        Finestre.apriModale("restaurant_details.fxml", valoreNonNullo(rd.getNome()),
                (RestaurantDetailsController ctrl) -> ctrl.setRestaurantData(
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
                ));
    }


    /**
     * Aggiorna la visibilità dei pulsanti in base al ruolo dell'utente.
     * <p>
     * Questa schermata è quella di ospiti e clienti: le funzioni da
     * ristoratore non compaiono più qui nemmeno nascoste, perché il
     * ristoratore ha una schermata tutta sua (la dashboard). Restano quindi
     * solo due casi da distinguere, ospite e cliente.
     *
     * @author Matteo Franguelli
     */
    private void aggiornaInterfaccia() {
        Session s = Session.getInstance();

        // Recuperiamo i permessi esatti
        boolean isGuest = s.isGuest();
        boolean puoRecensire = s.isCliente();
        boolean isLogged = !isGuest;

        // Login / Logout / Registrati
        if (bottoneLogin != null) { bottoneLogin.setVisible(isGuest); bottoneLogin.setManaged(isGuest); }
        if (bottoneRegistrati != null) { bottoneRegistrati.setVisible(isGuest); bottoneRegistrati.setManaged(isGuest); }
        if (bottoneLogout != null) { bottoneLogout.setVisible(isLogged); bottoneLogout.setManaged(isLogged); }

        // Etichetta Ruolo in alto
        if (etichettaRuolo != null) {
            if (isGuest) etichettaRuolo.setText("Ospite" + luogoCorrente(s));
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

        if (bottoneAggiungiRecensione != null) {
            bottoneAggiungiRecensione.setDisable(!puoRecensire);
        }
    }

    /**
     * Restituisce il luogo scelto dall'ospite all'ingresso, da mostrare
     * accanto alla dicitura "Ospite", oppure una stringa vuota se non c'è.
     *
     * @author Matteo Franguelli
     */
    private String luogoCorrente(Session s) {
        String citta = s.getCitta();
        return (citta == null || citta.isBlank()) ? "" : " · " + citta;
    }

    /**
     * Metodo chiamato sia da login che da register, permette l'aggiornamento dell'interfaccia
     * basandosi sulla Session
     * @author Matteo Franguelli
     */
    @Override
    public void onLoginSuccess() {
        Session session = Session.getInstance();

        // Il ristoratore non lavora sull'elenco dei ristoranti ma sulla propria
        // dashboard: appena accede, la finestra passa a quella schermata.
        if (session.isRistoratore()) {
            Finestre.cambiaVista(paginazione.getScene(), "dashboard.fxml");
            return;
        }

        aggiornaInterfaccia();
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
        Finestre.apriModale("login.fxml", "Login",
                (LoginController ctrl) -> ctrl.setParentController(this));
    }
    /**
     * Si occupa di mostrare la finestra di registrazione.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowRegister() {
        Finestre.apriModale("register.fxml", "Registrati",
                (RegisterController ctrl) -> ctrl.setParentController(this));
    }
    /**
     * Si occupa di disconnettere l'utente nel caso di click
     * sul pulsante logout.
     * @author Matteo Franguelli
     */
    @FXML
    private void onLogout() {
        Session.getInstance().logout();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Logout effettuato.");
        alert.showAndWait();

        // Si torna al benvenuto: uscendo si perde anche il luogo di ricerca,
        // che va richiesto di nuovo prima di rientrare nel catalogo
        Finestre.cambiaVista(paginazione.getScene(), "welcome.fxml");
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

        Finestre.apriModale("add_review.fxml", "Nuova recensione",
                (AddReviewController ctrl) -> {
                    ctrl.setRestaurant(selezionato);
                    ctrl.setRestaurantName(selezionato.getNome());
                });

        // La nuova recensione cambia la media mostrata sulle card
        aggiornaPaginazione();
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
     * Quando viene premuto il pulsante Preferiti mostra i ristoranti inseriti nei preferiti.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowFavorites() {
        Finestre.apriModale("favorites.fxml", "I miei preferiti");
    }

    /**
     * Quando viene premuto apre la finestra Filtro Avanzato.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowAdvancedFilter() {
        Finestre.apriModale("advanced_filter.fxml", "Filtro avanzato",
                (AdvancedFilterController ctrl) -> ctrl.setParent(this));
    }

    /**
     * Quando viene premuto apre la finestra che mostra le proprie recensioni.
     * @author Matteo Franguelli
     */
    @FXML
    private void onShowMyReviews() {
        Finestre.apriModale("my_reviews.fxml", "Le mie recensioni");

        // Recensioni modificate o eliminate cambiano la media sulle card
        aggiornaPaginazione();
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

        Finestre.apriModale("view_reviews.fxml", "Recensioni",
                (ViewReviewsController ctrl) -> ctrl.setRestaurant(selezionato));
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
     * Restituisce la stringa se non è null, altrimenti stringa vuota.
     * Utile per evitare NullPointerException nelle concatenazioni.
     * @author Matteo Franguelli
     */
    private String valoreNonNullo(String s) {
        return s == null ? "" : s;
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