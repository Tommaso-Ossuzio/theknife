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
 * Latitudine e longitudine di un luogo, usate per aprirlo su Google Maps.
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class CoordinateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id_coordinate;
    private double latitudine;
    private double longitudine;

    /**
     * Coordinate lette dal database.
     * @author Elia Toschi
     */
    public CoordinateDTO(int id_coordinate, double latitudine, double longitudine) {
        this.id_coordinate = id_coordinate;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    /**
     * Coordinate di un luogo non ancora salvato, quindi senza identificativo.
     * @author Celestino Resteghini
     */
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
