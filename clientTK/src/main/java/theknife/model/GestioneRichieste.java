package theknife.model;
import java.io.*;
import java.net.*;

/**
 * @author Celestino Resteghini
 */
public class GestioneRichieste {
    private static GestioneRichieste instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public GestioneRichieste() throws IOException {
        socket = new Socket("localhost", 8999);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Metodo per ottenere l'unica istanza condivisa (singleton)
     * @author Celestino Resteghini
     * @throws IOException
     */
    public static synchronized GestioneRichieste getInstance() throws IOException {
        if (instance == null || instance.socket.isClosed()) {
            instance = new GestioneRichieste();
        }
        return instance;
    }

    // TODO implementare metodo alla chiusura della gui

    /**
     * Metodo per chiudere la Socket alla chiusura della GUI
     * @author Celestino Resteghini
     */
    public void chiudiConnessione() {
        try {
            out.writeObject("END");
            out.flush();
            socket.close();
            System.out.println("Connessione col server chiusa");
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * Metodo per l'invio di un comando senza attesa della risposta
     * @param comando
     * @author Celestino Resteghini
     */
    public synchronized void inviaSoloComando(String comando) {
        try {
            out.writeObject(comando);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per l'invio di un comando con attesa della risposta
     * @param comando
     * @author Celestino Resteghini
     */
    public synchronized Object inviaEAttendi(String comando) {
        try {
            out.writeObject(comando);
            out.flush();
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Metodo per l'invio di un comando e un dato con attesa della risposta
     * @param comando
     * @param dato
     * @author Celestino Resteghini
     */
    public synchronized Object inviaEAttendi(String comando, Object dato) {
        try {
            out.writeObject(comando);
            out.writeObject(dato);
            out.flush();
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
