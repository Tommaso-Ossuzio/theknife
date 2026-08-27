package theknife.ui.javafx;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.CoordinateDTO;
import it.uninsubria.dto.FiltroRistoranteDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.RistoranteDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import theknife.utilities.Etichette;
import theknife.utilities.Finestre;
import theknife.utilities.Temi;
import theknife.utilities.Utility;

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
    @FXML private Button bottoneCerca;

    @FXML private ComboBox<String> campoLuogo;
    @FXML private TextField campoCucina;

    // Lista dei ristoranti mostrata dalla UI. I dati arrivano dal server come DTO.
    private final ObservableList<RistoranteDTO> ristoranti = FXCollections.observableArrayList();
    GestioneRistoranti gr = GestioneRistoranti.getInstance();

    /** Quante card mostrare in ogni pagina della griglia. */
    private static final int RISTORANTI_PER_PAGINA = 12;

    /** Ristorante attualmente selezionato con un click su una card. */
    private RistoranteDTO ristoranteSelezionato;

    /** Identificativo della ricerca più recente, per ignorare risposte vecchie. */
    private long ultimaRicerca;

    /** Card evidenziata, tenuta da parte per poterla deselezionare. */
    private Node cardSelezionata;

    /**
     * Esegue compiti di inizializzazione:
     * - Richiedere i ristoranti al server come DTO
     * - Imposta come la lista deve mostrare i ristoranti
     * - Imposta i pulsanti in base al ruolo (default: Ospite)
     * @author Matteo Franguelli
     */
    @FXML
    private void initialize() {
        inizializzaGriglia();
        Utility.confermaConInvio(bottoneCerca);
        Utility.completamentoCitta(campoLuogo);

        ristoranti.addListener((javafx.collections.ListChangeListener<RistoranteDTO>) c -> aggiornaPaginazione());

        //todo da cancellare caricaCatalogo();
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

        campoLuogo.getEditor().setText(citta);
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
        bottoneTema.setAccessibleText(Temi.descrizionePulsante());
    }

    /**
     * Mostra il catalogo dei ristoranti vicini al domicilio.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void caricaCatalogo() {
        Session session = Session.getInstance();
        applicaFiltro(new FiltroRistoranteDTO(
                session.getCitta(), null, null, null, null, null));
    }

    /**
     * Applica un filtro ricevuto dalla schermata principale o dal filtro
     * avanzato.
     *
     * <p>{@link GestioneRistoranti#filtraRistoranti(FiltroRistoranteDTO)} è
     * bloccante perché attende la risposta della socket. Per questo motivo la
     * chiamata viene eseguita dentro un {@link Task}; gli aggiornamenti della
     * lista e della paginazione avvengono poi sul thread JavaFX tramite gli
     * handler di completamento.</p>
     *
     * @param filtro criteri da inviare al server
     *
     * @author Michele Viselli
     */
    public void applicaFiltro(FiltroRistoranteDTO filtro) {
        if (filtro == null) {
            mostraErrore("Filtro non valido", "I criteri della ricerca non possono essere null.");
            return;
        }

        long identificativoRicerca = ++ultimaRicerca;

        Task<List<RistoranteDTO>> richiesta = new Task<>() {
            @Override
            protected List<RistoranteDTO> call() {
                return gr.filtraRistoranti(filtro);
            }
        };

        richiesta.setOnSucceeded(evento -> {
            if (identificativoRicerca == ultimaRicerca) {
                mostraRistoranti(richiesta.getValue());
            }
        });

        richiesta.setOnFailed(evento -> {
            if (identificativoRicerca != ultimaRicerca) return;
            ristoranti.clear();
            Throwable errore = richiesta.getException();
            String dettaglio = errore == null || errore.getMessage() == null
                    ? "Impossibile completare la ricerca."
                    : errore.getMessage();
            mostraErrore("Ricerca non disponibile", dettaglio);
        });

        Thread thread = new Thread(richiesta, "filtro-ristoranti");
        thread.setDaemon(true);
        thread.start();
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

        if (paginazione.getCurrentPageIndex() >= pagine) {
            paginazione.setCurrentPageIndex(0);
        } else {
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
    private Node creaCard(RistoranteDTO r) {
        VBox card = new VBox();
        card.getStyleClass().add("restaurant-card");

        Label nome = new Label(valoreNonNullo(r.getNome()));
        nome.setWrapText(true);
        nome.getStyleClass().add("restaurant-name");
        card.getChildren().add(nome);

        LuogoDTO luogo = r.getLuogo();
        CittaDTO citta = luogo == null ? null : luogo.getCitta();
        String indirizzo = (valoreNonNullo(luogo == null ? null : luogo.getVia())
                + ", " + valoreNonNullo(citta == null ? null : citta.getNome()))
                .replaceAll("(^, )|(, $)", "");
        if (!indirizzo.isBlank()) {
            card.getChildren().add(creaRigaCard("📍", indirizzo));
        }

        String prezzo = Etichette.formattaFasciaPrezzo(r.getFasciaPrezzo());
        if (!prezzo.isBlank()) {
            card.getChildren().add(creaRigaCard("💰", prezzo));
        }

        String sitoWeb = r.getSitoWeb();
        if (sitoWeb != null && !sitoWeb.isBlank() && !sitoWeb.equals("null")) {
            Hyperlink link = new Hyperlink(sitoWeb);
            link.getStyleClass().add("restaurant-link");
            link.setMaxWidth(300);
            card.getChildren().add(link);
        }

        Region spaziatore = new Region();
        VBox.setVgrow(spaziatore, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().add(spaziatore);

        FlowPane badge = new FlowPane();
        badge.getStyleClass().add("card-tags");

        badge.getChildren().add(Etichette.creaBadgeMedia(r));

        Label michelin = Etichette.creaBadgeMichelin(r);
        if (michelin != null) {
            badge.getChildren().add(michelin);
        }

        String cucina = r.getCucine() == null ? "" : String.join(", ", r.getCucine());
        if (!cucina.isBlank()) {
            badge.getChildren().add(Etichette.creaBadge(cucina, "tag"));
        }

        if (r.isDelivery()) {
            badge.getChildren().add(Etichette.creaBadge("Consegna", "tag-accent"));
        }
        if (r.isPrenotazioneOnline()) {
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
    private void selezionaRistorante(RistoranteDTO r, Node card) {
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
     * Apre una nuova finestra con i dettagli del ristorante selezionato.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    private void apriDettagliRistorante(RistoranteDTO rd) {
        Finestre.apriModale("restaurant_details.fxml", valoreNonNullo(rd.getNome()),
                (RestaurantDetailsController ctrl) -> {
                    try {
                        // Ora passiamo l'intero oggetto RistoranteDTO in un colpo solo!
                        ctrl.setRestaurantData(rd);
                    } catch (java.io.IOException e) {
                        System.err.println("Errore durante il caricamento dei dati del ristorante.");
                        e.printStackTrace();
                    }
                });
    }


    /**
     * Aggiorna la visibilità dei pulsanti in base al ruolo dell'utente.
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
                    campoLuogo.getEditor().setText(cittaUtente);
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

        RistoranteDTO selezionato = ristoranteSelezionato;

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
        String luogo = normalizza(campoLuogo.getEditor().getText());
        String cucina = normalizza(campoCucina.getText());
        if (luogo == null) {
            mostraErrore("Campo obbligatorio", "Devi inserire una città per effettuare la ricerca.");
            campoLuogo.requestFocus(); // Rimette il cursore nel campo vuoto
            return;
        }

        applicaFiltro(new FiltroRistoranteDTO(
                luogo, cucina, null, null, null, null));
    }

    /**
     * Metodo che permette di inizializzare i metodi di ricerca
     * @author Matteo Franguelli
     */
    @FXML
    public void onResetFilters() {
        if (campoLuogo != null) campoLuogo.getEditor().clear();
        if (campoCucina != null) campoCucina.clear();

        caricaCatalogo();

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

        aggiornaPaginazione();
    }



    /**
     * Permette la visualizzazione di recensioni dopo aver selezionato un ristorante.
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    @FXML
    private void onViewReviews() {
        RistoranteDTO selezionato = ristoranteSelezionato;

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
    public void mostraRistoranti(List<RistoranteDTO> nuovaLista) {
        ristoranti.clear();
        if (nuovaLista != null && !nuovaLista.isEmpty()) {
            ristoranti.addAll(nuovaLista);
        }
    }

    /**
     * Normalizza valori come luogo e cucina
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
};
