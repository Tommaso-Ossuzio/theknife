package theknife.model;

import it.uninsubria.dto.RecensioneDTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO Questa classe non ha info utili.. accede al file recensioni e calcola informazioni relative a quel file..
//da cancellare e creare query adatte per ottenere le informazioni necessarie + gestione aggiunta nuova recensione

/**
 * @author Elia Toschi
 * @author Matteo Franguelli
 */
public class GestioneRecensioni {
    public LinkedList<Recensione> recensioni;

    private static GestioneRecensioni instance;

    /** Cartella e nome del file in cui sono salvate le recensioni. */
    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_RECENSIONI = "recensioni.csv";

    /**
     * Indice delle recensioni raggruppate per ristorante.
     */
    private final Map<Integer, int[]> indice = new HashMap<>();

    /** Diventa vero dopo la prima lettura del file. */
    private boolean indiceCaricato = false;

    //costanti relative alle colonne del file recensioni
    /** Posizioni dei tre valori dentro il vettore dell'indice. */
    private static final int SOMMA = 0;
    private static final int CONTEGGIO = 1;
    private static final int SENZA_RISPOSTA = 2;

    public GestioneRecensioni() {
        recensioni = new LinkedList<>();
    }

    public void add(Recensione r)
    {
        if(!isPresente(r))
            recensioni.add(r);
    }

    /**
     * Restituisce l'istanza di GestioneRecensioni
     * @author Elia Toschi
     * @author Celestino Resteghini
     * @return instance
     */
    public static synchronized GestioneRecensioni getInstance() {
        if (instance == null) {
            instance = new GestioneRecensioni();
        }
        return instance;
    }

    /**
     * Verifica se la recensione è già presente nella lista
     * @param r
     * @author Celestino Resteghini
     * @return se la recensione è presente
     */
    public boolean isPresente(Recensione r)
    {
        return recensioni.contains(r);
    }

    /**
     * Restituisce tutte le recensioni presenti nella lista
     * @author Matteo Franguelli
     * @return
     */
    public LinkedList<Recensione> getRecensioni() { return recensioni; }




    /* =========================================================
       INDICE DELLE RECENSIONI PER RISTORANTE
       Il file recensioni.csv viene letto una volta sola e
       aggrvoltaegato in memoria: senza questo, mostrare la media
       nelle card significherebbe rileggere il file una
       per ogni ristorante mostrato.
       ========================================================= */

    /**
     * Rilegge il file delle recensioni e ricostruisce l'indice da zero.
     * Va chiamato dopo ogni scrittura del file, cioè quando viene aggiunta,
     * modificata o eliminata una recensione e quando un ristoratore risponde.
     * @author Matteo Franguelli
     */
    public synchronized void ricaricaIndice() {
        indice.clear();

        File file = new File(NOME_CARTELLA, NOME_FILE_RECENSIONI);
        if (!file.exists()) {
            indiceCaricato = true;
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank() || linea.toLowerCase().startsWith("n_stelle")) continue;

                String[] parti = linea.contains(";") ? linea.split(";") : linea.split(",");
                if (parti.length < 5) continue;

                try {
                    int stelle = Integer.parseInt(parti[0].trim());
                    int idRistorante = Integer.parseInt(parti[4].trim());

                    boolean haRisposta = parti.length > 5 && !parti[5].isBlank();

                    int[] valori = indice.computeIfAbsent(idRistorante, k -> new int[3]);
                    valori[SOMMA] += stelle;
                    valori[CONTEGGIO]++;
                    if (!haRisposta) valori[SENZA_RISPOSTA]++;

                } catch (NumberFormatException e) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        indiceCaricato = true;
    }

    /**
     * Costruisce l'indice se non è ancora stato costruito.
     * @author Matteo Franguelli
     */
    private synchronized void assicuraIndice() {
        if (!indiceCaricato) ricaricaIndice();
    }

    /**
     * Restituisce la media dei voti di un ristorante.
     *
     * @param idRistorante id del ristorante
     * @return la media, oppure -1 se il ristorante non ha ancora recensioni
     * @author Matteo Franguelli
     */
    public synchronized double getMedia(int idRistorante) {
        assicuraIndice();
        int[] valori = indice.get(idRistorante);
        if (valori == null || valori[CONTEGGIO] == 0) return -1;
        return (double) valori[SOMMA] / valori[CONTEGGIO];
    }

    /**
     * Restituisce quante recensioni ha ricevuto un ristorante.
     *
     * @param idRistorante id del ristorante
     * @author Matteo Franguelli
     */
    public synchronized int getConteggio(int idRistorante) {
        assicuraIndice();
        int[] valori = indice.get(idRistorante);
        return valori == null ? 0 : valori[CONTEGGIO];
    }
}
