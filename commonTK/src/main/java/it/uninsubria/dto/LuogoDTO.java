package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Indica il modello del luogo con cittaDTO e CoordinateDTO
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class LuogoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String via;
    private CittaDTO citta;
    private CoordinateDTO coordinate;

    public LuogoDTO(int id, String via, CittaDTO citta, CoordinateDTO coordinate) {
        this.id = id;
        this.via = via;
        this.citta = citta;
        this.coordinate = coordinate;
    }

    public LuogoDTO(String via, CittaDTO citta, CoordinateDTO coordinate) {
        this.via = via;
        this.citta = citta;
        this.coordinate = coordinate;
    }

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


