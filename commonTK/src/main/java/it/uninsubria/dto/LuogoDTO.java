package it.uninsubria.dto;

import java.io.Serializable;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
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

    public void setCoordinate(CoordinateDTO coordinate) {
        this.coordinate = coordinate;
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


