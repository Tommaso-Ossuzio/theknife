package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Rappresenta il DTO delle coordinate
 * @author Elia Toschi
 * @author Celestino Resteghini
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

    public CoordinateDTO(double latitudine, double longitudine) {
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
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
