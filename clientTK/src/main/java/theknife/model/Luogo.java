package theknife.model;
import static java.lang.Math.*;

//TODO da rivedere

/**
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * @author Elia Toschi
 */
public class Luogo {




    private String nazione;
    private String indirizzo;
    private String citta;
    private double latitudine;
    private double longitudine;


    public Luogo(String nazione, String indirizzo, String citta, double latitudine, double longitudine)
    {
        this.nazione = nazione;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }
    public String getNazione() { return nazione;}
    public String getIndirizzo() { return indirizzo;}
    public String getCitta() { return citta;}
    public double getLatitudine() { return latitudine;}
    public double getLongitudine() { return longitudine;}

    public boolean equals(Luogo l)
    {
        if(nazione != null)
            return this.nazione.equals(l.nazione) && this.indirizzo.equals(l.indirizzo) && this.citta.equals(l.citta) &&this.latitudine == l.latitudine && this.longitudine == l.longitudine ;
        return false;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof Luogo)
            return this.equals((Luogo)obj);
        return false;
    }
    /**
     * @author Celestino Resteghini
     * @author Matteo Franguelli
     * @author Elia Toschi
     */
    @Override
    public String toString()
    {
        return "Luogo: "+nazione+" "+indirizzo+" "+citta+" "+latitudine+" "+longitudine+"\n";
    }

}
