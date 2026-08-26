package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Rappresenta il DTO delle coordinate
 * @author Elia Toschi
 */
public class CoordinateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id_coordinate;
    private double latitudine;
    private double longitudine;

    public CoordinateDTO(int id_coordinate, double latitudine, double longitudine) {
        this.id_coordinate = id_coordinate;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    public int getId_coordinate() {
        return id_coordinate;
    }

    public void setId_coordinate(int id_coordinate) {
        this.id_coordinate = id_coordinate;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    @Override
    public String toString() {
        return "CoordinateDTO{" +
                "id_coordinate=" + id_coordinate +
                ", latitudine=" + latitudine +
                ", longitudine=" + longitudine +
                '}';
    }
}
