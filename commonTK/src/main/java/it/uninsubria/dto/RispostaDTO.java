package it.uninsubria.dto;

import java.io.Serializable;

public class RispostaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String testo;
    private int idRecensione;
    private RistoratoreDTO ristoratore;

    public RispostaDTO(String testo, int idRecensione, RistoratoreDTO ristoratore) {
        this.testo = testo;
        this.idRecensione = idRecensione;
        this.ristoratore = ristoratore;
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

    public RistoratoreDTO getRistoratore() {
        return ristoratore;
    }

    public void setRistoratore(RistoratoreDTO ristoratore) {
        this.ristoratore = ristoratore;
    }

    @Override
    public String toString() {
        return "RispostaDTO{" +
                "testo='" + testo + '\'' +
                ", idRecensione=" + idRecensione +
                ", ristoratore=" + ristoratore +
                '}';
    }
}
