package theknife.model;
import static java.lang.Math.*;

//TODO da rivedere

/**
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * @author Elia Toschi
 */
public class Luogo {

    private final double RAGGIOTERRESTRE_KM= 6371;


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
    /**
     * Verifica se un luogo è vicino entro 10 kilometri
     *
     * @param lat2 latitudine da confrontare
     * @param longi2 longitudine da confrontare
     * @return boolean true se < 10 km
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    public boolean checkDistance10KM(double lat2, double longi2)
    {
        double lat1Rad = toRadians(latitudine);
        double long1Rad= toRadians(longitudine);

        double lat2Rad = toRadians(lat2);
        double long2Rad= toRadians(longi2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = long2Rad - long1Rad;

        double a = pow(sin(dLat / 2), 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                        pow(sin(dLon / 2), 2);

        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        double distanzaKm=RAGGIOTERRESTRE_KM*c;
        if(distanzaKm<=10)
            return true;
        else
            return false;
    }
}
