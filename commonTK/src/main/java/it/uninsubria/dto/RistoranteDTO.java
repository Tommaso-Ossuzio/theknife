package it.uninsubria.dto;

import java.io.Serializable;
import java.util.List;


public class RistoranteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idRistorante;
    private String nome;
    private String telefono;
    private String sitoWeb;
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String fasciaPrezzo;
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
                         Integer numeroRecensioniSenzaRisposta) {
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
                ", cucine=" + cucine +
                ", luogo=" + luogo +
                ", ristoratore=" + ristoratore +
                ", mediaStelle=" + mediaStelle +
                ", numeroRecensioni=" + numeroRecensioni +
                ", numeroRecensioniSenzaRisposta=" + numeroRecensioniSenzaRisposta +
                '}';
    }
}
