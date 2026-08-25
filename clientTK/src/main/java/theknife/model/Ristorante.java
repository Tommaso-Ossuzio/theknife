package theknife.model;

import java.util.LinkedList;

//TODO da rivedere, mediaStelle e getNumRecensioni richiama il file

/**
 * @author Celestino Resteghini
 * @author Elia Toschi
 * @author Matteo Franguelli
 */
public class Ristorante {
    private String nome;
    private String numeroTelefono;
    private boolean delivery;
    private boolean booking;
    public double prezzo;
    public LinkedList<String> cucina;
    private Luogo luogo;
    private String website;
    private String link;
    private double awards;
    private  int id;
    private static int contatore=0;

    /**
     *
     * @param nome
     * @param numeroTelefono
     * @param delivery
     * @param booking
     * @param prezzo
     * @param cucina
     * @param luogo
     * @param website
     * @param link
     * @param awards
     *
     *
     * @author Celestino Resteghini
     * @author Elia Toschi
     * @author Matteo Franguelli
     */
    public Ristorante(String nome, String numeroTelefono, boolean delivery, boolean booking, double prezzo, LinkedList<String> cucina, Luogo luogo, String website, String link, double awards)
    {
        this.nome = nome;
        this.numeroTelefono = numeroTelefono;
        this.delivery = delivery;
        this.booking = booking;
        this.prezzo = prezzo;
        this.cucina = cucina;
        this.luogo = luogo;
        this.website = website;
        this.link = link;
        this.awards = awards;
        this.id =++contatore;

    }

    public String getNome(){return nome;}
    public String getN_tel(){return numeroTelefono;}
    public double getPrezzo() { return prezzo; }
    public boolean isDelivery() { return delivery; }
    public boolean isBooking() { return booking; }
    public LinkedList<String> getCucina(){return cucina;}
    public String getStringaCucina(){String s="";for(String c: cucina){s+=c+" ";}return s;}
    public Luogo getLuogo(){return luogo;}
    public String getWebsite() { return website; }
    public  int getId() { return id;  }

    /**
     * Restituisce la media delle stelle date dalle recensioni
     * @author Celestino Resteghini
     * @return sommaVoti/cont
     */
    public double getMediaStelle()
    {
        return GestioneRecensioni.getInstance().getMedia(id);
    }

    /**
     * Restituisce il numero delle recensioni
     * @author Celestino Resteghini
     * @return cont
     */
    public int getNumRecensioni()
    {
        return GestioneRecensioni.getInstance().getConteggio(id);
    }

    public double getAward()
    {
        return awards;
    }

    public String toString() {
        String cucine="";
        for(String c : cucina)
        {
            cucine+=c;
        }
        return "Ristorante: "+nome+" "+numeroTelefono+" "+(delivery ? "servizio di delivery disponibile" : "servizio di delivery non disponibile")+" "
                +(booking ? "servizio di prenotazione online disponibile" : "servizio di prenotazione online non disponibile")+" "+cucine+" "+luogo.toString()+"\n";
    }

    /**
     * @author Celestino Resteghini
     * @param r
     *
     */
    public boolean equals(Ristorante r) {
        return this.nome.equals(r.nome) && this.luogo.equals(r.luogo);
    }

    /**
     * @author Celestino Resteghini
     * @param r
     * @return this.equals((Ristorante) r)
     */
    @Override
    public boolean equals(Object r) {
        if(r instanceof Ristorante)
            return this.equals((Ristorante) r);
        return false;
    }
}
