package it.uninsubria;

import static java.lang.Math.*;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Classe utility.
 * <p>
 * Classe che contiene le funzionalità più utili
 *
 * @author Celestino Resteghini
 */
public class Utility {

    private final double RAGGIOTERRESTRE_KM= 6371;
    /**
     * Convertitore da prezzo a range di prezzo
     *
     * come indicato nel sito ufficiale della guida michelin:
     * € → meno di 35 €
     * €€ → tra 35 € e 60 €
     * €€€ → tra 60 € e 100 €
     * €€€€ → oltre 100 €
     *
     * descritti anche nel seguente modo:
     * € = “per tutte le tasche”
     * €€ = “costo ragionevole”
     * €€€ = “occasione speciale”
     * €€€€ = “piccola follia”
     *
     * @param prezzo
     * @autor  Celestino Resteghini
     */
    public String ConvertiPrezzoRange(Double prezzo)
    {
        if(prezzo <= 35)
            return "meno di 35 €";
        else if(prezzo > 35 && prezzo <= 60)
            return "tra 35 € e 60 €";
        else if(prezzo > 60 && prezzo <= 100)
            return "tra 60 € e 100 €";
        else if(prezzo > 100)
            return "oltre 100 €";
        return "ERRORE";
    }

    /**
     * Convertitore da simboli a range di prezzo
     *
     * come indicato nel sito ufficiale della guida michelin:
     * € → meno di 35 €
     * €€ → tra 35 € e 60 €
     * €€€ → tra 60 € e 100 €
     * €€€€ → oltre 100 €
     *
     * descritti anche nel seguente modo:
     * € = “per tutte le tasche”
     * €€ = “costo ragionevole”
     * €€€ = “occasione speciale”
     * €€€€ = “piccola follia”
     *
     * @param simboli
     * @autor  Celestino Resteghini
     */
    public String ConvertiStringaPrezzo(String simboli)
    {
        switch (simboli.length())
        {
            case 1:
                return "meno di 35 €";
            case 2:
                return "tra 35 € e 60 €";
            case 3:
                return "tra 60 € e 100 €";
            case 4:
                return "oltre 100 €";
        }
        return "ERRORE";
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
    //todo da vedere se implementarla
    public boolean checkDistance10KM(double lat, double longi, double lat2, double longi2)
    {
        double lat1Rad = toRadians(lat);
        double long1Rad= toRadians(longi);

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
