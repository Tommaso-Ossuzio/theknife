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

//TODO da rivedere

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
     * @author Elia Toschi
     */
    public LinkedList<Ristorante> getListaRistoranti() {return listaRistoranti;    }

    /**
     * Aggiunge un ristorante
     * @param ristorante
     */
    public void add(Ristorante ristorante)
    {
        listaRistoranti.add(ristorante);
    }

    /**
     * Restituisce un ristorante a partire dal suo indirizzo
     * @param indirizzo
     * @return r
     * @author Elia Toschi
     */
    public Ristorante getRistoranteDaIndirizzo(String indirizzo)
    {
        for(Ristorante r: listaRistoranti)
        {
            if(r.getLuogo().getIndirizzo().equals(indirizzo))
                return r;
        }
        return null;
    }

    public Ristorante getRistoranteDaNome(String nome)
    {
        for (Ristorante r : listaRistoranti) {
            if (nome.equalsIgnoreCase(r.getNome())) {
                return r;
            }
        }
        return null;

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

    /**
     * Filtra la lista di tutti i ristoranti con tutti i possibili parametri
     * @param luogo
     * @param cucina
     * @param prezzoMinore
     * @param prezzoMaggiore
     * @param delivery
     * @param booking
     * @param medStelle
     * @return r
     * @author Celestino Resteghini
     */
    public LinkedList<Ristorante> Filtro(String luogo, String cucina, double prezzoMinore, double prezzoMaggiore, boolean delivery, boolean booking, double medStelle)
    {
        LinkedList<Ristorante> r = null;

        if(luogo!=null && luogo.length()>0)
        {
            //Prendo il primo ristorante della città filtrata
            Optional<Ristorante> primoRist = listaRistoranti.stream().filter(x -> x.getLuogo().getCitta().equalsIgnoreCase(luogo)).findFirst();

            if (primoRist.isPresent()) { //Se esiste un ristorante in quella città, prendo lat e long
                double lat1 = primoRist.get().getLuogo().getLatitudine();
                double long1 = primoRist.get().getLuogo().getLongitudine();

                //Filtro tutti i ristoranti (anche di altre città) entro 10 km
                r = listaRistoranti.stream().filter(x -> {
                    Luogo l = x.getLuogo();
                    return l.checkDistance10KM(lat1, long1);
                }).collect(Collectors.toCollection(LinkedList::new));
            }
            else
            {
                System.out.println("=== [MANCANO RISTORANTI IN QUEL LUOGO] ===");
                return r;
            }

            if (cucina != null && cucina.length()>0)//rimozione dei ristoranti con cucine diverse da quella selezionata
            {
                r.removeIf(x -> !x.getCucina().contains(cucina));
            }

            if (prezzoMinore >= 0 && prezzoMaggiore >= 0)//rimozione dei ristoranti con prezzo medio non compreso tra min e max
            {
                r.removeIf(x -> !(x.prezzo > prezzoMinore && x.prezzo < prezzoMaggiore));
            } else if (prezzoMinore >= 0)//rimozione dei ristoranti con prezzo medio minore del min
            {
                r.removeIf(x -> x.prezzo < prezzoMinore);
            } else if (prezzoMaggiore >= 0) //rimozione dei ristoranti con prezzo medio maggiore del max
            {
                r.removeIf(x -> x.prezzo > prezzoMaggiore);
            }

            if (delivery) //rimozione dei ristoranti che non hanno il servizio di delivery
            {
                r.removeIf(x -> x.isDelivery() == false);
            }

            if (booking) //rimozione dei ristoranti che non hanno il servizio di booking
            {
                r.removeIf(x -> x.isBooking() == false);
            }

            if (medStelle > 0) {
                r.removeIf(x -> x.getMediaStelle() < medStelle); //rimozione dei ristoranti che non hanno medStelle minore
            }
        }
        else
        {
            System.out.println("=== [MANCA IL LUOGO] ===");
        }
        return r;
    }


    /* =========================================================
       CARICAMENTO DEL CATALOGO DAL FILE CSV
       Il caricamento sta qui e non più nel controller della
       schermata principale perché serve anche alla schermata di
       benvenuto, che deve proporre l'elenco delle città prima
       ancora che la lista dei ristoranti venga mostrata.
       ========================================================= */

    /**
     * Legge il catalogo dei ristoranti dal file CSV e riempie
     * {@link #listaRistoranti}. Se il catalogo è già stato caricato non fa
     * nulla, così può essere invocato senza timore da più schermate.
     * <p>
     * Il metodo è bloccante e legge circa 17.700 righe: va richiamato da un
     * thread separato, mai dal thread grafico.
     *
     * @author Matteo Franguelli
     * @author Celestino Resteghini
     */
    public synchronized void caricaDaCsv() {
        if (caricato) return;

        LinkedList<Ristorante> bufferTemporaneo = new LinkedList<>();
        InputStream is = null;

        try {
            File fileEsterno = new File(NOME_CARTELLA, NOME_FILE_DATI);

            if (fileEsterno.exists()) {
                System.out.println("Caricamento dati da: " + fileEsterno.getAbsolutePath());
                is = new FileInputStream(fileEsterno);
            }

            if (is == null) {
                System.err.println("ERRORE: " + NOME_FILE_DATI + " non trovato.");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String linea = br.readLine();
                if (linea != null && linea.toLowerCase().contains("name")) {
                    linea = br.readLine();
                }

                while (linea != null) {
                    aggiungiDaRigaCsv(linea, bufferTemporaneo);
                    linea = br.readLine();
                }
            }
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        listaRistoranti.addAll(bufferTemporaneo);
        caricato = true;
    }

    /**
     * Indica se il catalogo è già stato letto dal file.
     * @author Matteo Franguelli
     */
    public synchronized boolean isCaricato() {
        return caricato;
    }

    /**
     * Restituisce le città presenti nel catalogo con quanti ristoranti
     * ciascuna contiene, ordinate alfabeticamente ignorando maiuscole e
     * minuscole.
     * <p>
     * Serve alla schermata di benvenuto: l'ospite sceglie da qui il luogo da
     * cui sta cercando, così non può digitare una città che nel catalogo non
     * esiste e non si ritrova mai davanti a una lista vuota.
     *
     * @author Matteo Franguelli
     */
    public synchronized Map<String, Integer> getCittaConConteggio() {
        Map<String, Integer> citta = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Ristorante r : listaRistoranti) {
            if (r.getLuogo() == null) continue;
            String nome = r.getLuogo().getCitta();
            if (nome == null || nome.isBlank()) continue;

            citta.merge(nome.trim(), 1, Integer::sum);
        }
        return citta;
    }

    /**
     * Converte una singola riga CSV in un oggetto Ristorante
     * e lo aggiunge alla lista indicata.
     * @author Celestino Resteghini
     */
    private void aggiungiDaRigaCsv(String linea, LinkedList<Ristorante> destinazione) {
        if (linea == null || linea.isBlank()) return;

        String[] parti = dividiCsv(linea);

        String nome = pulisci(parti[0]);

        String[] s = parti[1].split(",");
        String indirizzo = pulisci(s[0]);

        s = parti[2].split(",");

        String citta   = s.length > 0 ? pulisci(s[0]) : null;
        String nazione = s.length > 1 ? pulisci(s[1]) : null;

        double prezzo = pulisci(parti[3]).length() * 20; //ogni simbolo = 20€

        LinkedList<String> tipoCucina = new LinkedList<>();
        s = parti[4].split(",");

        // Aggiungi ogni elemento alla LinkedList
        for (String e : s) {
            tipoCucina.add(pulisci(e));
        }

        // Coordinate (attenzione agli errori di formato)
        double latitudine = 0;
        double longitudine = 0;

        try { longitudine = Double.parseDouble(pulisci(parti[5])); } catch (NumberFormatException ignored) {}

        try { latitudine = Double.parseDouble(pulisci(parti[6])); } catch (NumberFormatException ignored) {}

        String num_tel = pulisci(parti[7]);

        // Link e info aggiuntive
        String link = pulisci(parti[8]);

        String website = pulisci(parti[9]);

        s = parti[10].split(" ");

        double award = -1;
        if (s.length > 1) {
            String a = s[1].substring(0, 4);

            if (parti[10] != null && a.equals("Star")) {
                award = Double.parseDouble(pulisci(s[0]));
            } else {
                award = -1;
            }
        }
        // Se nel CSV non c'è un link, generiamo un link a Google Maps
        if (link == null || link.isBlank()) {
            link = "https://www.google.com/maps?q="
                    + inUrl(nome) + "+" + inUrl(indirizzo) + "+" + inUrl(citta);
        }

        boolean delivery = false;
        boolean booking = false;

        if (parti.length > 14 && "true".equalsIgnoreCase(parti[14]))
            delivery = true;

        if (parti.length > 15 && "true".equalsIgnoreCase(parti[15]))
            booking = true;

        Ristorante r = new Ristorante(nome, num_tel, delivery, booking, prezzo, tipoCucina,
                new Luogo(nazione, indirizzo, citta, latitudine, longitudine), website, link, award);

        destinazione.add(r);
    }

    /**
     * Divide una riga CSV in campi, gestendo i campi tra doppi apici.
     * @author Matteo Franguelli
     */
    private String[] dividiCsv(String linea) {
        return linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * Rimuove eventuali doppi apici e spazi inutili.
     * @author Matteo Franguelli
     */
    private String pulisci(String s) {
        if (s == null) return "";
        return s.replace("\"", "").trim();
    }

    /**
     * Converte uno spazio in '+' per poter usare la stringa in una URL.
     * @author Matteo Franguelli
     */
    private String inUrl(String s) {
        return s == null ? "" : s.trim().replace(" ", "+");
    }

}
