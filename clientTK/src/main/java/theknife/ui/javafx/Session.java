package theknife.ui.javafx;

//TODO da rivedere, non avremo più 4 tipi di utenti, ma solo 3

/**
 * Gestisce lo stato dell'utente attualmente loggato nell'applicazione.
 * È un singleton: esiste una sola Session per tutta l'app.
 * Mantiene sia il ruolo principale (per visualizzazione) che i permessi specifici.
 * @author Matteo Franguelli
 */
public class Session {

    /**
     * Ruoli possibili dell'utente (usati principalmente per etichette UI).
     * CLIENTE → utente standard
     * RISTORATORE → gestore di ristoranti
     * GUEST → utente non autenticato
     * @author Matteo Franguelli
     */
    public enum Role {
        CLIENTE,
        RISTORATORE,
        GUEST
    }

    // Istanza singleton della sessione
    private static Session instance;

    private String username;
    private String citta;
    private Role ruolo;
    private boolean permessiCliente;
    private boolean permessiRistoratore;

    /**
     * Costruttore privato: la sessione parte come "ospite" senza permessi.
     * @author Matteo Franguelli
     */
    private Session() {
        this.ruolo = Role.GUEST;
        this.permessiCliente = false;
        this.permessiRistoratore = false;
    }

    /**
     * Restituisce l'unica istanza della Session,
     * creandola se non esiste ancora.
     * @author Matteo Franguelli
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public String getCitta() {
        return citta;
    }

    /**
     * Effettua il login impostando username e ruolo principale.
     * I permessi specifici vengono resettati in base al ruolo,
     * ma è consigliabile usare setPermessi() subito dopo per precisione.
     * @author Matteo Franguelli
     */
    public void login(String username, Role ruolo) {
        this.username = username;
        this.ruolo = (ruolo == null ? Role.GUEST : ruolo);

        // Imposta permessi di default in base al ruolo semplice
        // (Verranno sovrascritti se si usa setPermessi)
        if (this.ruolo == Role.GUEST) {
            this.permessiCliente = false;
            this.permessiRistoratore = false;
        } else if (this.ruolo == Role.CLIENTE) {
            this.permessiCliente = true;
            this.permessiRistoratore = false;
        } else if (this.ruolo == Role.RISTORATORE) {
            this.permessiRistoratore = true;
        }
    }

    /**
     * Imposta esplicitamente i permessi dell'utente.
     * Utile quando un utente ha il doppio ruolo (es. ristoratore che vuole recensire).
     *
     * @param isCliente true se l'utente può lasciare recensioni e avere preferiti
     * @param isRistoratore true se l'utente può aggiungere ristoranti
     * @author Matteo Franguelli
     */
    public void setPermessi(boolean isCliente, boolean isRistoratore) {
        this.permessiCliente = isCliente;
        this.permessiRistoratore = isRistoratore;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    /**
     * Restituisce true se l'utente ha i permessi da Cliente (es. recensioni).
     * @author Matteo Franguelli
     */
    public boolean isCliente() {
        return permessiCliente;
    }

    /**
     * Restituisce true se l'utente ha i permessi da ristoratore (es. aggiunta ristoranti).
     * @author Matteo Franguelli
     */
    public boolean isRistoratore() {
        return permessiRistoratore;
    }

    /**
     * Effettua il logout e torna allo stato di ospite.
     * @author Matteo Franguelli
     */
    public void logout() {
        this.username = null;
        this.ruolo = Role.GUEST;
        this.permessiCliente = false;
        this.permessiRistoratore = false;
        this.citta=null;
    }

    /**
     * Ritorna true se l’utente è un ospite (non loggato).
     * @author Matteo Franguelli
     */
    public boolean isGuest() {
        return ruolo == Role.GUEST;
    }

    /**
     * Ritorna true se l’utente NON è ospite.
     * @author Matteo Franguelli
     */
    public boolean isAuthenticated() {
        return ruolo != Role.GUEST;
    }

    /**
     * Imposta lo stato "autenticato / non autenticato".
     * Se impostato a false → logout.
     * Se impostato a true e il ruolo attuale è GUEST → diventa CLIENTE.
     * @author Matteo Franguelli
     */
    public void setAuthenticated(boolean autenticato) {
        if (!autenticato) {
            logout();
        } else {
            // se non aveva un ruolo definito, diventa un cliente normale
            if (this.ruolo == Role.GUEST) {
                this.ruolo = Role.CLIENTE;
                this.permessiCliente = true;
            }
        }
    }

    /**
     * Aggiorna lo username dell’utente loggato.
     * @author Matteo Franguelli
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Aggiorna solo il ruolo principale dell’utente loggato.
     * @author Matteo Franguelli
     */
    public void setRole(Role ruolo) {
        this.ruolo = (ruolo == null ? Role.GUEST : ruolo);
    }

    /**
     * Restituisce lo username dell'utente attualmente loggato.
     * @author Matteo Franguelli
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce il ruolo principale dell'utente.
     * @author Matteo Franguelli
     */
    public Role getRole() {
        return ruolo;
    }
}