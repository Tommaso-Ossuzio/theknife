/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * @author Elia Toschi
 */
public class Utility {

    private static final double RAGGIOTERRESTRE_KM= 6371;

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
    public static String ConvertiStringaPrezzo(String simboli)
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
    public static boolean checkDistance10KM(double lat, double longi, double lat2, double longi2)
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

    /**
     * Calcola l'hash SHA-256 di una stringa.
     * @author Matteo Franguelli
     */
    public static String calcolaSha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
