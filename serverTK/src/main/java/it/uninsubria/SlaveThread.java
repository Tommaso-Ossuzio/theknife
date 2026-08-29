package it.uninsubria;

import it.uninsubria.dao.*;
import it.uninsubria.dto.*;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread che si occupa di gestire le rischieste degli utenti
 * @author Celestino Resteghini
 * @author Elia Toschi
 * @author Michele Viselli
 */
public class SlaveThread extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    protected SlaveThread(Socket s) throws IOException, ClassNotFoundException {
        socket = s;
        out = new ObjectOutputStream(s.getOutputStream());
        in = new ObjectInputStream(s.getInputStream());
    }

    UtenteDAO utenteDAO = new UtenteDAO();
    RecensioneDAO recensioneDAO = new RecensioneDAO();
    RistoranteDAO ristoranteDAO = new RistoranteDAO();
    PreferitiDAO preferitiDAO = new PreferitiDAO();
    LuogoDAO luogoDAO = new LuogoDAO();

    /**
     * Metodo del thread per la gestione di tutte le richieste (seguendo il protocollo)
     * @author Celestino Resteghini
     */
    public void run() {
        String comando;
        try {
            while (true) {
                comando = (String) in.readObject();
                if (comando.equals("END")) break;

                switch (comando) {
                    case "LOG" -> gestisciLogin();
                    case "REG" -> gestisciRegistrazione();
                    case "FILTRO" -> gestisciFiltro();
                    case "REC" -> gestisciRecensioni();
                    case "AGG-REC" -> gestisciAggiuntaRecensione();
                    case "VIS-REC" -> gestisciVisualizzaRecensioni();
                    case "MOD-REC" -> gestisciModificaRecensione();
                    case "ELIM-REC" -> gestisciEliminaRecensione();
                    case "VIS-PREF" -> gestisciVisualizzaPreferiti();
                    case "ELIM-PREF" -> gestisciEliminaPreferito();
                    case "AGG-PREF" -> gestisciAggiungiPreferito();
                    case "MAPS" -> gestisciCoordinateMaps();
                    case "DOM" -> gestisciDomicilio();
                    case "RIST" -> gestisciRistoranti();
                    case "AGG-RIST" -> gestisciAggiungiRistorante();
                    case "REC-NO-RISP" -> gestisciRecensioniSenzaRisposta();
                    case "RISP-REC" -> gestisciRispostaRecensione();
                    case "CITTA" -> gestisciCitta();
                    case "CUC" -> gestisciCucine();
                    case "ID" -> gestisciId();
                    default -> System.err.println("Comando sconosciuto: " + comando);
                }
            }
            System.out.println("END");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { }
        }
    }

    /**
     * Metodo per ottenere la connessione al database
     * @author Celestino Resteghini
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getTargetUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
    }

    /**
     * Metodo per il controllo delle credenziali d'accesso nel login
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciLogin() throws IOException, ClassNotFoundException {
        AuthDTO credenziali = (AuthDTO) in.readObject();
        System.out.println("LOG: ricevuto: " + credenziali.toString());
        HashMap<String, Boolean> hm = new HashMap<>();
        Boolean m=false;

        Boolean esito = false;
        try (Connection conn = getConnection()) {
            if(utenteDAO.eseguiLogin(conn,credenziali)){
                esito = true;
            }
            m = utenteDAO.isRistoratore(conn, credenziali.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        hm.put("LOG", esito);
        hm.put("is_ristoratore", m);

        System.out.println("LOG: invio: " + hm);
        out.writeObject(hm);
        out.flush();
    }

    /**
     * Metodo per effettuare la registrazione di un nuovo utente
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRegistrazione() throws IOException, ClassNotFoundException {
        UtenteDTO nuovoUtente = (UtenteDTO) in.readObject();
        System.out.println("REG: ricevuto: " + nuovoUtente.toString());
        Boolean esito = false;
        try (Connection conn = getConnection()) {
            if(utenteDAO.registraUtente(conn,nuovoUtente,nuovoUtente.getPassword()))
                esito = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("REG: invio: " + esito);
        out.writeObject(esito);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di ristoranti che soddisfano le specifiche indicate nel filtro
     * @author Michele Viselli
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciFiltro() throws IOException, ClassNotFoundException {
        FiltroRistoranteDTO specificheFiltro = (FiltroRistoranteDTO) in.readObject();
        System.out.println("FILTRO: ricevuto: " + specificheFiltro.toString());
        LinkedList<RistoranteDTO> ristoranti;

        try (Connection conn = getConnection()) {

            ristoranti = new LinkedList<>(ristoranteDAO.filtraRistoranti(conn, specificheFiltro));
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca FILTRO");
            e.printStackTrace();
            ristoranti = new LinkedList<>();
        }

        System.out.println("FILTRO: invio: lista ristoranti (" + ristoranti.size() + " elementi)");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di recensioni relativa ad un ristorante
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    private void gestisciRecensioni() throws IOException, ClassNotFoundException {
        int idRistorante = (int) in.readObject();
        System.out.println("REC: ricevuto: " + idRistorante);
        LinkedList<RecensioneDTO> recensioni = new LinkedList<>();

        try (Connection conn = getConnection()) {
            recensioni = new LinkedList<>(
                    recensioneDAO.getRecensioniPerRistorante(conn, idRistorante)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per aggiungere una recensione
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    private void gestisciAggiuntaRecensione() throws IOException, ClassNotFoundException {
        RecensioneDTO recensione = (RecensioneDTO) in.readObject();
        System.out.println("AGG-REC: ricevuto: " + recensione.toString());

        try (Connection conn = getConnection()) {
            recensioneDAO.inserisciRecensione(conn,recensione);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di recensioni scritte da un determinato utente
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    private void gestisciVisualizzaRecensioni() throws IOException, ClassNotFoundException {
        int idUtente = (int) in.readObject();
        System.out.println("VIS-REC: ricevuto: " + idUtente);
        LinkedList<RecensioneDTO> recensioni = null;

        try (Connection conn = getConnection()) {
            recensioni =(LinkedList<RecensioneDTO>) recensioneDAO.getRecensioniDaUtente(conn,idUtente);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(recensioni == null)
            recensioni = new LinkedList<>();

        System.out.println("VIS-REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per modficare una recensione
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    private void gestisciModificaRecensione() throws IOException, ClassNotFoundException {
        RecensioneDTO recensione = (RecensioneDTO) in.readObject();
        System.out.println("MOD-REC: ricevuto: " + recensione.toString());
        LinkedList<RecensioneDTO> recensioni = null;
        try (Connection conn = getConnection()) {
            recensioneDAO.modificaRecensione(conn,recensione);
            recensioni = (LinkedList<RecensioneDTO>) recensioneDAO.getRecensioniDaUtente(conn,recensione.getIdUtente());
        } catch (SQLException e) {
            e.printStackTrace();

        }
        if(recensioni == null)
            recensioni = new LinkedList<>();
        System.out.println("MOD-REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per eliminare una recensione
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciEliminaRecensione() throws IOException, ClassNotFoundException {
        int idRecensione = (int) in.readObject();
        System.out.println("ELIM-REC: ricevuto: " + idRecensione);

        try (Connection conn = getConnection()) {
            recensioneDAO.eliminaRecensione(conn,idRecensione);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di ristoranti preferiti di un utente
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciVisualizzaPreferiti() throws IOException, ClassNotFoundException {
        int idUtente = (int) in.readObject();
        System.out.println("VIS-PREF: ricevuto: " + idUtente);
        LinkedList<RistoranteDTO> ristoranti =null;

        try (Connection conn = getConnection()) {
            ristoranti = (LinkedList<RistoranteDTO>) ristoranteDAO.getRistorantiPreferiti(conn,idUtente);
        } catch (SQLException e) {
            e.printStackTrace();

        }
        if(ristoranti == null)
            ristoranti = new LinkedList<>();

        System.out.println("VIS-PREF: invio: lista ristoranti");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per eliminare il ristorante dai preferiti di un utente
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciEliminaPreferito() throws IOException, ClassNotFoundException {
        HashMap<String, Integer> idMap = (HashMap<String, Integer>) in.readObject();
        int idUtente = idMap.get("idUtente");
        int idRistorante = idMap.get("idRistorante");
        System.out.println("ELIM-PREF: ricevuto: utente " + idUtente + ", ristorante " + idRistorante);

        try (Connection conn = getConnection()) {
            preferitiDAO.rimuoviPreferito(conn,idUtente,idRistorante);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per aggiungere il ristorante ai preferiti di un utente
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciAggiungiPreferito() throws IOException, ClassNotFoundException {
        HashMap<String, Integer> idMap = (HashMap<String, Integer>) in.readObject();
        int idUtente = idMap.get("idUtente");
        int idRistorante = idMap.get("idRistorante");
        System.out.println("AGG-PREF: ricevuto: utente " + idUtente + ", ristorante " + idRistorante);

        try (Connection conn = getConnection()) {
            preferitiDAO.aggiungiPreferito(conn,idUtente,idRistorante);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere le coordinate di un ristorante
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciCoordinateMaps() throws IOException, ClassNotFoundException {
        int idRistorante = (int) in.readObject();
        System.out.println("MAPS: ricevuto: " + idRistorante);
        CoordinateDTO coordinate =null;

        try (Connection conn = getConnection()) {
            coordinate= luogoDAO.getCoordinateRistorante(conn,idRistorante);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(coordinate==null){
            coordinate= new CoordinateDTO(1,1,1);
        }

        System.out.println("MAPS: invio: " + coordinate.toString());
        out.writeObject(coordinate);
        out.flush();
    }

    /**
     * Metodo per inviare il domicilio
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciDomicilio() throws IOException, ClassNotFoundException {
        String email = (String) in.readObject();
        System.out.println("DOM: ricevuto: " + email);
        String citta = "";
        try (Connection conn = getConnection()) {
            citta = luogoDAO.getDomicilio(conn, email);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("DOM: invio domicilio: " + citta);
        out.writeObject(citta);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di ristoranti di un ristoratore
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRistoranti() throws IOException, ClassNotFoundException {
        int idRistoratore = (int) in.readObject();
        System.out.println("RIST: ricevuto: " + idRistoratore);
        LinkedList<RistoranteDTO> ristoranti = null;
        try (Connection conn = getConnection()) {
            ristoranti =(LinkedList<RistoranteDTO>) ristoranteDAO.getRistorantiDelRistoratore(conn,idRistoratore);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(ristoranti==null){
            ristoranti= new LinkedList<>();
        }

        System.out.println("RIST: invio: lista ristoranti");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per aggiungere un ristorante
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciAggiungiRistorante() throws IOException, ClassNotFoundException {
        RistoranteDTO ristorante = (RistoranteDTO) in.readObject();
        System.out.println("AGG-RIST: ricevuto: " + ristorante.toString());

        try (Connection conn = getConnection()) {
            ristoranteDAO.inserisciRistorante(conn,ristorante);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di recensioni senza risposta
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRecensioniSenzaRisposta() throws IOException, ClassNotFoundException {
        int idRistoratore = (int) in.readObject();
        System.out.println("REC-NO-RISP: ricevuto: " + idRistoratore);
        LinkedList<RecensioneDTO> recensioni = null;

        try (Connection conn = getConnection()) {
            recensioni = (LinkedList<RecensioneDTO>) recensioneDAO.getRecensioniSenzaRisposta(conn,idRistoratore);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(recensioni==null){
            recensioni= new LinkedList<>();
        }

        System.out.println("REC-NO-RISP: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per rispondere ad una recensione
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRispostaRecensione() throws IOException, ClassNotFoundException {
        RispostaDTO risposta = (RispostaDTO) in.readObject();
        System.out.println("RISP-REC: ricevuto: " + risposta.toString());

        try (Connection conn = getConnection()) {
           recensioneDAO.inserisciRisposta(conn,risposta);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di tutte le città
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     */
    private void gestisciCitta() throws IOException {
        List<String> citta = null;
        try(Connection conn= getConnection()) {
            citta= ristoranteDAO.getCitta(conn);
        }catch(SQLException e){
            e.printStackTrace();
        }

        System.out.println("CITTA: invio: lista citta");
        out.writeObject(citta);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di tutte le cucine
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @throws IOException
     */
    private void gestisciCucine() throws IOException {
        List<String> cucine =null;
        try(Connection conn= getConnection())
        {
            cucine = ristoranteDAO.getCucine(getConnection());
        }catch(SQLException e){
            e.printStackTrace();
        }

        System.out.println("CUC: invio: lista cucine");
        out.writeObject(cucine);
        out.flush();
    }

    /**
     * Metodo per inviare l'id una volta ricevuta l'email del client
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciId() throws IOException, ClassNotFoundException {
        String email = (String) in.readObject();
        System.out.println("ID: ricevuto: " + email.toString());
        int id = -1;
        try(Connection conn= getConnection())
        {
            id = utenteDAO.getIdUtenteDaMail(conn, email);
        }catch(SQLException e){
            e.printStackTrace();
        }
        System.out.println("ID: invio id:" + id);
        out.writeObject(id);
        out.flush();
    }
}
