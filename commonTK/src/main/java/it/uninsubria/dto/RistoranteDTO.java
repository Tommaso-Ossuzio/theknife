package it.uninsubria.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Rappresenta il modello DTO del ristorante e contiene luogoDTO, RistoratoreDTO
 * @author  Elia Toschi
 * @author Michele Viselli
 * @author Celestino Resteghini
 */
public class RistoranteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idRistorante;
    private String nome;
    private String telefono;
    private String sitoWeb;
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String fasciaPrezzo;
    private int stelleMichelin;
    private List<String> cucine;
    private LuogoDTO luogo;
    private RistoratoreDTO ristoratore;
    private Double mediaStelle;
    private Integer numeroRecensioni;
    private Integer numeroRecensioniSenzaRisposta;



    public RistoranteDTO(int idRistorante, String nome, String telefono, String sitoWeb,
                         boolean delivery, boolean prenotazioneOnline, String fasciaPrezzo,
                         List<String> cucine, LuogoDTO luogo, RistoratoreDTO ristoratore,
                         Double mediaStelle, Integer numeroRecensioni,
                         Integer numeroRecensioniSenzaRisposta, int stelleMichelin) {
        this.idRistorante = idRistorante;
        this.nome = nome;
        this.telefono = telefono;
        this.sitoWeb = sitoWeb;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.fasciaPrezzo = fasciaPrezzo;
        this.cucine = cucine;
        this.luogo = luogo;
        this.ristoratore = ristoratore;
        this.mediaStelle = mediaStelle;
        this.numeroRecensioni = numeroRecensioni;
        this.numeroRecensioniSenzaRisposta = numeroRecensioniSenzaRisposta;
        this.stelleMichelin = stelleMichelin;
    }

    public RistoranteDTO(String nome, String nazione, String citta, String indirizzo, double latitudine, double longitudine, String telefono, double prezzo, List<String> cucine, String sitoWeb, boolean delivery, boolean prenotazioneOnline, int stelleMichelin, RistoratoreDTO ristoratore) {
        this.nome = nome;
        this.luogo = new LuogoDTO(indirizzo,new CittaDTO(citta, nazione), new CoordinateDTO(latitudine, longitudine));
        this.telefono = telefono;
        this.fasciaPrezzo = ConvertiPrezzoRange(prezzo);
        this.cucine = cucine;
        this.sitoWeb = sitoWeb;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.stelleMichelin = stelleMichelin;
        this.ristoratore = ristoratore;
    }

    //TODO creato per comando mod rec
    public RistoranteDTO(int idRistorante){
        this.idRistorante = idRistorante;
        //TODO creare un metodo che ottenga il numero di recensioni e la media stelle di un ristorante partendo dall'id
        this.numeroRecensioni = numeroRecensioni;
        this.mediaStelle = mediaStelle;
    }

    public int getStelleMichelin() {
        return stelleMichelin;
    }

    public void setStelleMichelin(int stelleMichelin) {
        this.stelleMichelin = stelleMichelin;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRistorante) {
        this.idRistorante = idRistorante;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSitoWeb() {
        return sitoWeb;
    }

    public void setSitoWeb(String sitoWeb) {
        this.sitoWeb = sitoWeb;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isPrenotazioneOnline() {
        return prenotazioneOnline;
    }

    public void setPrenotazioneOnline(boolean prenotazioneOnline) {
        this.prenotazioneOnline = prenotazioneOnline;
    }

    public String getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public void setFasciaPrezzo(String fasciaPrezzo) {
        this.fasciaPrezzo = fasciaPrezzo;
    }

    public List<String> getCucine() {
        return cucine;
    }

    public void setCucine(List<String> cucine) {
        this.cucine = cucine;
    }

    public LuogoDTO getLuogo() {
        return luogo;
    }

    public void setLuogo(LuogoDTO luogo) {
        this.luogo = luogo;
    }

    public RistoratoreDTO getRistoratore() {
        return ristoratore;
    }

    public void setRistoratore(RistoratoreDTO ristoratore) {
        this.ristoratore = ristoratore;
    }

    public Double getMediaStelle() {
        return mediaStelle;
    }

    public void setMediaStelle(Double mediaStelle) {
        this.mediaStelle = mediaStelle;
    }

    public Integer getNumeroRecensioni() {
        return numeroRecensioni;
    }

    public void setNumeroRecensioni(Integer numeroRecensioni) {
        this.numeroRecensioni = numeroRecensioni;
    }

    public Integer getNumeroRecensioniSenzaRisposta() {
        return numeroRecensioniSenzaRisposta;
    }

    public void setNumeroRecensioniSenzaRisposta(Integer numeroRecensioniSenzaRisposta) {
        this.numeroRecensioniSenzaRisposta = numeroRecensioniSenzaRisposta;
    }

    @Override
    public String toString() {
        return "RistoranteDTO{" +
                "idRistorante=" + idRistorante +
                ", nome='" + nome + '\'' +
                ", telefono='" + telefono + '\'' +
                ", sitoWeb='" + sitoWeb + '\'' +
                ", delivery=" + delivery +
                ", prenotazioneOnline=" + prenotazioneOnline +
                ", fasciaPrezzo='" + fasciaPrezzo + '\'' +
                ", stelleMichelin=" + stelleMichelin +
                ", cucine=" + cucine +
                ", luogo=" + luogo +
                ", ristoratore=" + ristoratore +
                ", mediaStelle=" + mediaStelle +
                ", numeroRecensioni=" + numeroRecensioni +
                ", numeroRecensioniSenzaRisposta=" + numeroRecensioniSenzaRisposta +
                '}';
    }

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
    public static String ConvertiPrezzoRange(Double prezzo)
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
}
