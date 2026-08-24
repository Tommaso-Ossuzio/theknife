package it.uninsubria;
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

    protected SlaveThread(Socket s) throws IOException {
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
                if (comando.equals("LOG")) {
                    AuthDTO credenziali = (AuthDTO) in.readObject();
                    //TODO controllo se le credenziali sono corrette
                    if(true) { //se il controllo è andato a buon fine
                        out.writeObject(true);
                        out.flush();
                    } else {
                        out.writeObject(false);
                        out.flush();
                    }
                }
                if (comando.equals("REG")) {
                    UtenteDTO nuovoUtente = (UtenteDTO) in.readObject();
                    //TODO controllare nel db che l'utente non sia già presente e nel caswo registrarlo
                    if(true) { //se è avvenuta la registrazione
                        out.writeObject(true);
                        out.flush();
                    } else {
                        out.writeObject(false); //se esiste già
                        out.flush();
                    }
                }
                if(comando.equals("FILTRO")) {
                    FiltroRistoranteDTO specificheFiltro = (FiltroRistoranteDTO) in.readObject();
                    LinkedList<RistoranteDTO> ristoranti;

                    try (Connection conn = DriverManager.getConnection(
                            DatabaseConfig.getTargetUrl(),
                            DatabaseConfig.getUser(),
                            DatabaseConfig.getPassword())) {
                        RistoranteDAO ristoranteDAO = new RistoranteDAO();
                        ristoranti = new LinkedList<>(
                                ristoranteDAO.filtraRistoranti(conn, specificheFiltro));
                    } catch (SQLException e) {
                        System.err.println("Errore durante la ricerca FILTRO");
                        e.printStackTrace();
                        // Manteniamo il protocollo sincronizzato restituendo
                        // comunque una risposta al client.
                        ristoranti = new LinkedList<>();
                    }

                    out.writeObject(ristoranti);
                    out.flush();
                }
                if(comando.equals("REC")) {
                    int idRistorante = (int) in.readObject();
                    //TODO prendere dal db tutte le recensioni relative all'id del ristorante passato dal client
                    LinkedList<RecensioneDTO> recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
                    out.writeObject(recensioni);
                    out.flush();
                }
                if(comando.equals("AGG-REC")) {
                    RecensioneDTO recensione = (RecensioneDTO) in.readObject();
                    //TODO aggiungere recensione nel db
                }
                if(comando.equals("VIS-REC")) {
                    int idUtente = (int) in.readObject();
                    //TODO prendere dal db tutte le recensioni relative all'id dell'utente passato dal client
                    LinkedList<RecensioneDTO> recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
                    out.writeObject(recensioni);
                    out.flush();
                }
                if(comando.equals("MOD-REC")) {
                    RecensioneDTO recensione = (RecensioneDTO) in.readObject();
                    //TODO modificare nel db la recensione con i dati forniti dal client
                    LinkedList<RecensioneDTO> recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
                    out.writeObject(recensioni);
                    out.flush();
                }
                if(comando.equals("ELIM-REC")) {
                    RecensioneDTO recensione = (RecensioneDTO) in.readObject();
                    //TODO eliminare nel db la recensione con i dati forniti dal client
                }
                if(comando.equals("VIS-PREF")) {
                    int idUtente = (int) in.readObject();
                    //TODO prendere dal db tutti i ristoranti preferiti del client
                    LinkedList<RistoranteDTO> ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
                    out.writeObject(ristoranti);
                    out.flush();
                }
                if(comando.equals("ELIM-PREF")) {
                    /*
                    * Lato client scrivere:
                    * HashMap<String, Integer> id = new HashMap<>();
                    * id.put("idUtente", idUtente);
                    * id.put("idRistorante", idRistorante)
                    * LinkedList<RistoranteDTO> ristoranti = (LinkedList<RistoranteDTO>) GestioneRichieste.getInstance().inviaEAttendi("ELIM-PREF", id);
                    * */
                    HashMap<String, Integer> id = (HashMap<String, Integer>) in.readObject();
                    int idUtente = id.get("idUtente");
                    int idRistorante = id.get("idRistorante");
                    //TODO eliminare il ristorante dai preferiti
                }
                if(comando.equals("AGG-PREF")) {
                    HashMap<String, Integer> id = (HashMap<String, Integer>) in.readObject();
                    int idUtente = id.get("idUtente");
                    int idRistorante = id.get("idRistorante");
                    //TODO aggiungere il ristorante ai preferiti
                }
                if(comando.equals("MAPS")) {
                    int idRistorante = (int) in.readObject();
                    //TODO cercare coordinate del ristorante passato dal client
                    CoordinateDTO coordinate= new CoordinateDTO(1, 1,1); //TODO sostituire le coordinate con quelle prese dal db
                    out.writeObject(coordinate);
                    out.flush();
                }
                if(comando.equals("RIST")) {
                    int idRistoratore = (int) in.readObject();
                    //TODO prendere dal db tutti i ristoranti associati al ristoratore
                    LinkedList<RistoranteDTO> ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
                    out.writeObject(ristoranti);
                    out.flush();
                }
                if(comando.equals("AGG-RIST")) {
                    RistoranteDTO ristorante = (RistoranteDTO) in.readObject();
                    //TODO aggiungere il ristorante nel db associandolo al ristoratore
                    LinkedList<RistoranteDTO> ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
                    out.writeObject(ristoranti);
                    out.flush();
                }
                if(comando.equals("REC-NO-RISP")) {
                    int idRistoratore = (int) in.readObject();
                    //TODO cercare nel db le recensioni senza risposta relative ai ristoranti del ristoratore
                    LinkedList<RecensioneDTO> recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista delle recensioni richieste
                    out.writeObject(recensioni);
                    out.flush();
                }
                if(comando.equals("RISP-REC")) {
                    RecensioneDTO recensione = (RecensioneDTO) in.readObject();
                    //TODO aggiungere nel db la risposta alla recensione
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { }
        }
    }

}
