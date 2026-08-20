package it.uninsubria;

/**
 * Classe utility.
 * <p>
 * Classe che contiene le funzionalità più utili
 *
 * @author Celestino Resteghini
 */
public class Utility {

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
}
