package it.uninsubria.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class RecensioneDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idRecensione;
    private String testo;
    private int numeroStelle;
    private String  data;
    private String ora;


    private UtenteDTO utente;
    private RispostaDTO risposta;

    public RecensioneDTO(int idRecensione, String testo, int numeroStelle, String data, String ora, UtenteDTO utente, RispostaDTO risposta) {
        this.idRecensione = idRecensione;
        this.testo = testo;
        this.numeroStelle = numeroStelle;
        this.data = data;
        this.ora = ora;
        this.utente = utente;
        this.risposta = risposta;
    }

    public int getIdRecensione() {
        return idRecensione;
    }

    public void setIdRecensione(int idRecensione) {
        this.idRecensione = idRecensione;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public int getNumeroStelle() {
        return numeroStelle;
    }

    public void setNumeroStelle(int numeroStelle) {
        this.numeroStelle = numeroStelle;
    }

    public String getOra() {
        return ora;
    }

    public void setOra(String ora) {
        this.ora = ora;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public UtenteDTO getUtente() {
        return utente;
    }

    public void setUtente(UtenteDTO utente) {
        this.utente = utente;
    }

    public RispostaDTO getRisposta() {
        return risposta;
    }

    public void setRisposta(RispostaDTO risposta) {
        this.risposta = risposta;
    }

    @Override
    public String toString() {
        return "RecensioneDTO{" +
                "idRecensione=" + idRecensione +
                ", testo='" + testo + '\'' +
                ", numeroStelle=" + numeroStelle +
                ", data='" + data + '\'' +
                ", ora='" + ora + '\'' +
                ", utente=" + utente +
                ", risposta=" + risposta +
                '}';
    }
}
