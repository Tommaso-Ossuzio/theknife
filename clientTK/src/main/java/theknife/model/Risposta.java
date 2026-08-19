package theknife.model;

//TODO da rivedere, si dovrebbe poter fare solo una risposta, non più di una

/**
 * La classe è una risposta ad una recensione
 * @author Elia Toschi
 */
public class Risposta {

    int idRistoratore;
    String text;

    /**
     *
     * @param idRistoratore
     * @param text
     * @author Elia Toschi
     */
    public Risposta(int idRistoratore, String text)
    {
        this.text = text;
    }

    public String getTextString()
    {return text;}
    /**
     * stampa
     * @return stringa
     * @author Elia Toschi
     */
    @Override
    public String toString() {
        return "Recensione: "+ idRistoratore+" "+text+"\n";
    }

    /**
     * @author Elia Toschi
     * @param r
     * @return boolean
     */
    public boolean equals(Risposta r) {
        return this.idRistoratore==r.idRistoratore && this.text ==(r.text);
    }

    /**
     * equals
     * @param r
     * @return boolean
     * @author Elia Toschi
     */
    @Override
    public boolean equals(Object r) {
        if(r instanceof Risposta)
            return this.equals((Risposta) r);
        return false;
    }



}
