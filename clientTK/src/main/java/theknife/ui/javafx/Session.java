package theknife.ui.javafx;

//TODO da rivedere, non avremo più 4 tipi di utenti, ma solo 3

import theknife.model.GestioneRichieste;

import java.io.IOException;

/**
 * Stato dell'utente collegato: ruolo, permessi, città e identificativo.
 * È un singleton, ne esiste una sola per tutta l'applicazione.
 * @author Matteo Franguelli
 */
public class Session {

    /**
     * Ruolo con cui l'utente sta usando l'applicazione.
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
    private int id;

    /**
     * La sessione parte sempre come ospite, senza permessi.
     * @author Matteo Franguelli
     */
    private Session() {
        this.ruolo = Role.GUEST;
        this.permessiCliente = false;
        this.permessiRistoratore = false;
    }

    /**
     * Restituisce l'unica sessione, creandola al primo utilizzo.
     * @author Matteo Franguelli
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Restituisce la città da cui l'utente sta cercando.
     * @author Matteo Franguelli
     */
    public String getCitta() {
        return citta;
    }

    /**
     * Registra l'accesso di un utente, azzerando i dati di quello precedente.
     * @param username email dell'utente, null per un ospite
     * @param ruolo ruolo con cui entra
     * @author Matteo Franguelli
     * @author Michele Viselli
     */
    public void login(String username, Role ruolo) throws IOException {
        this.username = username;
        this.ruolo = (ruolo == null ? Role.GUEST : ruolo);
        // Città e ID appartengono all'utente precedente (o alla precedente
        // sessione guest): non devono sopravvivere a un nuovo accesso.
        this.citta = null;
        this.id = 0;

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
     * Chiede al server identificativo e domicilio dell'utente appena entrato.
     * @throws IOException se la connessione al server non è disponibile
     * @author Michele Viselli
     */
    public void aggiornaDatiUtente() throws IOException {
        if (!isAuthenticated() || username == null || username.isBlank()) return;

        GestioneRichieste richieste = GestioneRichieste.getInstance();

        Object rispostaId = richieste.inviaEAttendi("ID", username);
        this.id = rispostaId instanceof Integer idUtente ? idUtente : 0;

        Object rispostaDomicilio = richieste.inviaEAttendi("DOM", username);
        if (rispostaDomicilio instanceof String domicilio
                && !domicilio.isBlank()
                && !domicilio.equalsIgnoreCase("ERRORE")) {
            this.citta = domicilio.trim();
        } else {
            this.citta = null;
        }
    }

    /**
     * Imposta i permessi dell'utente, indipendenti dal ruolo mostrato a schermo.
     * @param isCliente true se l'utente può lasciare recensioni e avere preferiti
     * @param isRistoratore true se l'utente può aggiungere ristoranti
     * @author Matteo Franguelli
     */
    public void setPermessi(boolean isCliente, boolean isRistoratore) {
        this.permessiCliente = isCliente;
        this.permessiRistoratore = isRistoratore;
    }

    /**
     * Imposta la città da cui l'utente sta cercando.
     * @author Matteo Franguelli
     */
    public void setCitta(String citta) {
        this.citta = citta;
    }

    /**
     * Indica se l'utente può lasciare recensioni e salvare preferiti.
     * @author Matteo Franguelli
     */
    public boolean isCliente() {
        return permessiCliente;
    }

    /**
     * Indica se l'utente può inserire ristoranti e rispondere alle recensioni.
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
        this.id = 0;
    }

    /**
     * Indica se l'utente non ha effettuato l'accesso.
     * @author Matteo Franguelli
     */
    public boolean isGuest() {
        return ruolo == Role.GUEST;
    }

    /**
     * Indica se l'utente ha effettuato l'accesso.
     * @author Matteo Franguelli
     */
    public boolean isAuthenticated() {
        return ruolo != Role.GUEST;
    }

    /**
     * Restituisce lo username dell'utente attualmente loggato.
     * @author Matteo Franguelli
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce l'identificativo dell'utente collegato.
     * @return l'identificativo, 0 se nessuno ha effettuato l'accesso
     * @author Celestino Resteghini
     */
    public int getID() {
        return id;
    }
}
