package theknife.model;

import java.util.Date;

//TODO da rivedere

/**
 * @author Elia Toschi
 */
public class Recensione {
    private int numeroStelle;
    private String text;
    private Date data;
    private int id_utente_reg;
    private int id_ristorante;
    private int id;
    private static int contatore=0;
    private Risposta risposta;

    /**
     * @author Elia Toschi
     * @param numeroStelle
     * @param text
     * @param id_utente
     * @param id_ristorante
     */
    public Recensione(int numeroStelle, String text, int id_utente, int id_ristorante)
    {
        if(text.length()>=300)
            text=text.substring(0,300);
        this.numeroStelle = numeroStelle;
        this.text = text;
        this.id_utente_reg = id_utente;
        this.id_ristorante = id_ristorante;
        this.data = new Date();
        this.id = ++contatore;
    }

    public int getNumeroStelle() {return numeroStelle;}
    public String getText() {return text;}
    public int getIdUtente() {return id_utente_reg;}
    public int get_id_Ristorante() {return id_ristorante;}
    public Risposta getRisposta() {return risposta;}
    public Date getData() {return data;}

    public void setData(Date date)
    {
        this.data= date;
    }

    public void setRisposta(Risposta risposta) {
        this.risposta = risposta;
    }

    /**
     * @author Elia Toschi
     */
    @Override
    public String toString() {
        GestioneRistoranti g = new GestioneRistoranti();
        return "Recensione: "+ numeroStelle+" "+text+" "+id_utente_reg+" "+g.getRistorante(id_ristorante)+"\n";
    }
    /**
     * @author Elia Toschi
     */
    public boolean equals(Recensione r) {
        return this.data.equals(r.data) && this.id_utente_reg ==(r.id_utente_reg) && this.id_ristorante==(r.id_ristorante);
    }
    /**
     * @author Matteo Franguelli
     * @author Elia Toschi
     */
    @Override
    public boolean equals(Object r) {
        if(r instanceof Recensione)
            return this.equals((Recensione)r);
        return false;
    }
}
