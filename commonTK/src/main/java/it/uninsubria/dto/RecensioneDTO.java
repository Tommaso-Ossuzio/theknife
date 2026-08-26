package it.uninsubria.dto;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Rappresenta i dati della recensione
 * @author Elia Toschi
 */
public class RecensioneDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idRistorante;
    private String testo;
    private int numeroStelle;
    private String  data;
    private String ora;
    private int idUtente;
    private RispostaDTO risposta;
    private int idRecensione ;

    public RecensioneDTO( String testo, int numeroStelle, String data, String ora, int idUtente,int idRistorante, RispostaDTO risposta) {
        this.idRistorante = idRistorante;
        this.testo = testo;
        this.numeroStelle = numeroStelle;
        this.data = data;
        this.ora = ora;
        this.idUtente = idUtente;
        this.risposta = risposta;

    }

    public RecensioneDTO(int idRistorante, String testo, int numeroStelle, String data, String ora, int idUtente, RispostaDTO risposta, int idRecensione) {
        this.idRistorante = idRistorante;
        this.testo = testo;
        this.numeroStelle = numeroStelle;
        this.data = data;
        this.ora = ora;
        this.idUtente = idUtente;
        this.risposta = risposta;
        this.idRecensione = idRecensione;
    }

    public int getIdRecensione() {
        return idRecensione;
    }

    public void setIdRecensione(int idRecensione) {
        this.idRecensione = idRecensione;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRecensione) {
        this.idRistorante = idRistorante;
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

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(UtenteDTO utente) {
        this.idUtente = idUtente;
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
                "idRistorante=" + idRistorante +
                ", testo='" + testo + '\'' +
                ", numeroStelle=" + numeroStelle +
                ", data='" + data + '\'' +
                ", ora='" + ora + '\'' +
                ", utente=" + idUtente +
                ", risposta=" + risposta +
                '}';
    }
}
