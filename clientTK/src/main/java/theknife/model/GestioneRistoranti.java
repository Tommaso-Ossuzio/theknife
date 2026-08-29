package theknife.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

//TODO da cancellare le informazioni estrapolate dal file e modificata aggiunta ristorante

/**
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * @author Elia Toschi
 */
public class GestioneRistoranti {
    public LinkedList<Ristorante> listaRistoranti;
    private static GestioneRistoranti instance;

    /** Cartella e nome del file con il catalogo dei ristoranti. */
    private static final String NOME_CARTELLA = "data";
    private static final String NOME_FILE_DATI = "michelin_my_maps.csv";

    /** Diventa vero quando il catalogo è stato letto dal file. */
    private boolean caricato = false;

    public GestioneRistoranti()
    {
        listaRistoranti = new LinkedList<>();
    }

    public static synchronized GestioneRistoranti getInstance() {
        if (instance == null) {
            instance = new GestioneRistoranti();
        }
        return instance;
    }

    /**
     * Aggiunge un ristorante
     * @param ristorante
     */
    public void add(Ristorante ristorante)
    {
        listaRistoranti.add(ristorante);
    }

    /**
     * Restituisce un ristorante avendo l'id
     * @param id
     * @return
     * @author Elia Toschi
     */
    public Ristorante getRistorante(int id)
    {
        for(Ristorante r : listaRistoranti )
            if(r.getId() == id)
                return r;
        return null;
    }
}
