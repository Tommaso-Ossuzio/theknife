package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Recensione lasciata da un cliente, con l'eventuale risposta del ristoratore.
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
    private String nomeRistorante;

    /**
     * Recensione letta dal database quando il suo identificativo non serve.
     * @author Elia Toschi
     */
    public RecensioneDTO( String testo, int numeroStelle, String data, String ora, int idUtente,int idRistorante, RispostaDTO risposta) {
        this.idRistorante = idRistorante;
        this.testo = testo;
        this.numeroStelle = numeroStelle;
        this.data = data;
        this.ora = ora;
        this.idUtente = idUtente;
        this.risposta = risposta;

    }

    /**
     * Recensione completa letta dal database.
     * @author Elia Toschi
     */
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

    /**
     * Recensione appena scritta dal cliente, da inviare al server.
     * @author Elia Toschi
     */
    public RecensioneDTO(String testo, int numeroStelle, int idUtente, int idRistorante){
        this.testo = testo;
        this.numeroStelle = numeroStelle;
        this.idUtente = idUtente;
        this.idRistorante = idRistorante;
    }

    /**
     * Recensione modificata: al server servono solo il nuovo voto e il nuovo testo.
     * @author Elia Toschi
     */
    public RecensioneDTO(int idRecensione, String testo, int numeroStelle, int idUtente)
    {
        this.idUtente = idUtente;
        this.idRecensione = idRecensione;
        this.testo = testo;
        this.numeroStelle = numeroStelle;
    }

    public int getIdRecensione() {
        return idRecensione;
    }

    public void setIdRecensione(int idRecensione) {
        this.idRecensione = idRecensione;
    }

    public String getNomeRistorante() {
        return nomeRistorante;
    }

    public void setNomeRistorante(String nomeRistorante) {
        this.nomeRistorante = nomeRistorante;
    }

    public int getIdRistorante() {
        return idRistorante;
    }

    public String getTesto() {
        return testo;
    }

    public int getNumeroStelle() {
        return numeroStelle;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public RispostaDTO getRisposta() {
        return risposta;
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
