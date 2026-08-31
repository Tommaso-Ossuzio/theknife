/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Risposta pubblica di un ristoratore a una recensione.
 * @author Elia Toschi
 */
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

    public RistoratoreDTO getRistoratore() {
        return ristoratore;
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
