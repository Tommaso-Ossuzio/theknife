package it.uninsubria;
import it.uninsubria.dao.PreferitiDAO;
import it.uninsubria.dao.RistoranteDAO;
import it.uninsubria.dto.*;
import it.uninsubria.dto.UtenteDTO;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Celestino Resteghini
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

    /**
     * Metodo per la gestione di tutte le richieste (seguendo il protocollo)
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
                    case "RIST" -> gestisciRistoranti();
                    case "AGG-RIST" -> gestisciAggiungiRistorante();
                    case "REC-NO-RISP" -> gestisciRecensioniSenzaRisposta();
                    case "RISP-REC" -> gestisciRispostaRecensione();
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
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciLogin() throws IOException, ClassNotFoundException {
        AuthDTO credenziali = (AuthDTO) in.readObject();
        System.out.println("LOG: ricevuto: " + credenziali.toString());

        boolean esito = false;
        try (Connection conn = getConnection()) {
            //TODO controllo se le credenziali sono corrette
            if(true)
                esito = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("LOG: invio: " + esito);
        out.writeObject(esito);
        out.flush();
    }

    /**
     * Metodo per effettuare la registrazione di un nuovo utente
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRegistrazione() throws IOException, ClassNotFoundException {
        UtenteDTO nuovoUtente = (UtenteDTO) in.readObject();
        System.out.println("REG: ricevuto: " + nuovoUtente.toString());

        boolean esito = false;
        try (Connection conn = getConnection()) {
            //TODO controllare nel db che l'utente non sia già presente e nel caso registrarlo
            if(true)
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
            RistoranteDAO ristoranteDAO = new RistoranteDAO();
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
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRecensioni() throws IOException, ClassNotFoundException {
        int idRistorante = (int) in.readObject();
        System.out.println("REC: ricevuto: " + idRistorante);
        LinkedList<RecensioneDTO> recensioni = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO prendere dal db tutte le recensioni relative all'id del ristorante passato dal client
            recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per aggiungere una recensione
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciAggiuntaRecensione() throws IOException, ClassNotFoundException {
        RecensioneDTO recensione = (RecensioneDTO) in.readObject();
        System.out.println("AGG-REC: ricevuto: " + recensione.toString());

        try (Connection conn = getConnection()) {
            //TODO aggiungere recensione nel db
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di recensioni scritte da un determinato utente
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciVisualizzaRecensioni() throws IOException, ClassNotFoundException {
        int idUtente = (int) in.readObject();
        System.out.println("VIS-REC: ricevuto: " + idUtente);
        LinkedList<RecensioneDTO> recensioni = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO prendere dal db tutte le recensioni relative all'id dell'utente passato dal client
            recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("VIS-REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per modficare una recensione
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciModificaRecensione() throws IOException, ClassNotFoundException {
        RecensioneDTO recensione = (RecensioneDTO) in.readObject();
        System.out.println("MOD-REC: ricevuto: " + recensione.toString());
        LinkedList<RecensioneDTO> recensioni = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO modificare nel db la recensione con i dati forniti dal client
            recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("MOD-REC: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per eliminare una recensione
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciEliminaRecensione() throws IOException, ClassNotFoundException {
        int idRecensione = (int) in.readObject();
        System.out.println("ELIM-REC: ricevuto: " + idRecensione);

        try (Connection conn = getConnection()) {
            //TODO eliminare nel db la recensione con i dati forniti dal client
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere la lista di ristoranti preferiti di un utente
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciVisualizzaPreferiti() throws IOException, ClassNotFoundException {
        int idUtente = (int) in.readObject();
        System.out.println("VIS-PREF: ricevuto: " + idUtente);
        LinkedList<RistoranteDTO> ristoranti = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO prendere dal db tutti i ristoranti preferiti del client
            ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("VIS-PREF: invio: lista ristoranti");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per eliminare il ristorante dai preferiti di un utente
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciEliminaPreferito() throws IOException, ClassNotFoundException {
        /*
         * Lato client scrivere:
         * HashMap<String, Integer> id = new HashMap<>();
         * id.put("idUtente", idUtente);
         * id.put("idRistorante", idRistorante)
         * LinkedList<RistoranteDTO> ristoranti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("ELIM-PREF", id);
         * */
        HashMap<String, Integer> idMap = (HashMap<String, Integer>) in.readObject();
        int idUtente = idMap.get("idUtente");
        int idRistorante = idMap.get("idRistorante");
        System.out.println("ELIM-PREF: ricevuto: utente " + idUtente + ", ristorante " + idRistorante);

        try (Connection conn = getConnection()) {
            //TODO eliminare il ristorante dai preferiti
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per aggiungere il ristorante ai preferiti di un utente
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciAggiungiPreferito() throws IOException, ClassNotFoundException {
        HashMap<String, Integer> idMap = (HashMap<String, Integer>) in.readObject();
        int idUtente = idMap.get("idUtente");
        int idRistorante = idMap.get("idRistorante");
        System.out.println("AGG-PREF: ricevuto: utente " + idUtente + ", ristorante " + idRistorante);

        try (Connection conn = getConnection()) {
            //TODO aggiungere il ristorante ai preferiti
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per ottenere le coordinate di un ristorante
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciCoordinateMaps() throws IOException, ClassNotFoundException {
        int idRistorante = (int) in.readObject();
        System.out.println("MAPS: ricevuto: " + idRistorante);
        CoordinateDTO coordinate = new CoordinateDTO(1, 1,1);

        try (Connection conn = getConnection()) {
            //TODO cercare coordinate del ristorante passato dal client
            coordinate= new CoordinateDTO(1, 1,1); //TODO sostituire le coordinate con quelle prese dal db
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("MAPS: invio: " + coordinate.toString());
        out.writeObject(coordinate);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di ristoranti di un ristoratore
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRistoranti() throws IOException, ClassNotFoundException {
        int idRistoratore = (int) in.readObject();
        System.out.println("RIST: ricevuto: " + idRistoratore);
        LinkedList<RistoranteDTO> ristoranti = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO prendere dal db tutti i ristoranti associati al ristoratore
            ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("RIST: invio: lista ristoranti");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per aggiungere un ristorante
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciAggiungiRistorante() throws IOException, ClassNotFoundException {
        RistoranteDTO ristorante = (RistoranteDTO) in.readObject();
        System.out.println("AGG-RIST: ricevuto: " + ristorante.toString());
        LinkedList<RistoranteDTO> ristoranti = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO aggiungere il ristorante nel db associandolo al ristoratore
            ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("AGG-RIST: invio: lista ristoranti aggiornata");
        out.writeObject(ristoranti);
        out.flush();
    }

    /**
     * Metodo per ottenere la lista di recensioni senza risposta
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRecensioniSenzaRisposta() throws IOException, ClassNotFoundException {
        int idRistoratore = (int) in.readObject();
        System.out.println("REC-NO-RISP: ricevuto: " + idRistoratore);
        LinkedList<RecensioneDTO> recensioni = new LinkedList<>();

        try (Connection conn = getConnection()) {
            //TODO cercare nel db le recensioni senza risposta relative ai ristoranti del ristoratore
            recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("REC-NO-RISP: invio: lista recensioni");
        out.writeObject(recensioni);
        out.flush();
    }

    /**
     * Metodo per rispondere ad una recensione
     * @author Celestino Resteghini
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void gestisciRispostaRecensione() throws IOException, ClassNotFoundException {
        RecensioneDTO recensione = (RecensioneDTO) in.readObject();
        System.out.println("RISP-REC: ricevuto: " + recensione.toString());

        try (Connection conn = getConnection()) {
            //TODO aggiungere nel db la risposta alla recensione
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
