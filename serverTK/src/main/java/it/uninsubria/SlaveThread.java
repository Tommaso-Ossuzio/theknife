package it.uninsubria;
import it.uninsubria.dto.*;
import it.uninsubria.dto.UtenteDTO;

import java.io.*;
import java.net.*;
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
                    String credenziali = (String) in.readObject(); //ricevute come username-psw ???
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
                    String specificheFilto = (String) in.readObject(); //Forse va fatta una classe DTO ad hoc per questa ???
                    //TODO prendere dal db tutti i ristoranti che rientrano nelle specifiche fornite dal client
                    LinkedList<RistoranteDTO> ristoranti = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
                    out.writeObject(ristoranti);
                    out.flush();
                }
                if(comando.equals("RECENSIONI")) {
                    String idRistorante = (String) in.readObject();
                    //TODO prendere dal db tutte le recensioni relative all'id del ristorante passato dal client
                    LinkedList<RecensioneDTO> recensioni = new LinkedList<>(); //TODO sostituire la lista vuota con la lista dei ristoranti richiesti
                    out.writeObject(recensioni);
                    out.flush();
                }

                //TODO completare protocollo
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { }
        }
    }

}
