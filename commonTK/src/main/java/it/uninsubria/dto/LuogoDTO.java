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
 * Indirizzo completo di un ristorante o di un utente: via, città e coordinate.
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class LuogoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String via;
    private CittaDTO citta;
    private CoordinateDTO coordinate;

    /**
     * Luogo completo, come arriva dal database.
     * @author Elia Toschi
     */
    public LuogoDTO(int id, String via, CittaDTO citta, CoordinateDTO coordinate) {
        this.id = id;
        this.via = via;
        this.citta = citta;
        this.coordinate = coordinate;
    }

    /**
     * Luogo di un ristorante appena inserito, non ancora salvato.
     * @author Celestino Resteghini
     */
    public LuogoDTO(String via, CittaDTO citta, CoordinateDTO coordinate) {
        this.via = via;
        this.citta = citta;
        this.coordinate = coordinate;
    }

    /**
     * Domicilio di un utente, di cui interessa soltanto la città.
     * @author Celestino Resteghini
     */
    public LuogoDTO(CittaDTO citta) {
        this.citta = citta;
    }

    public String getVia() {
        return via;
    }

    public CittaDTO getCitta() {
        return citta;
    }

    public void setCitta(CittaDTO citta) {
        this.citta = citta;
    }

    public CoordinateDTO getCoordinate() {
        return coordinate;
    }

    @Override
    public String toString() {
        return "LuogoDTO{" +
                "id=" + id +
                ", via='" + via + '\'' +
                ", citta=" + citta +
                ", coordinate=" + coordinate +
                '}';
    }
}


