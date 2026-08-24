package it.uninsubria.dao;

import it.uninsubria.dto.RistoranteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

public class PreferitiDAO {
    /**
     * Aggiunge un ristorante preferito
     * @param conn
     * @param idUtente
     * @param idRistorante
     * @author Elia Toschi
     */
    public void aggiungiPreferito(Connection conn, int idUtente, int idRistorante)
    {
        String sql="INSERT INTO PREFERITO (id_utente, id_ristorante) VALUES (?,?)";
        try(PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, idUtente);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Rimuove un ristorante preferito
     * @param conn
     * @param idUtente
     * @param idRistorante
     * @author Elia Toschi
     */
    public void rimuoviPreferito(Connection conn, int idUtente, int idRistorante) {
        String sql = "DELETE FROM PREFERITO P WHERE P.id_utente= ? AND P.id_ristorante=? ";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
