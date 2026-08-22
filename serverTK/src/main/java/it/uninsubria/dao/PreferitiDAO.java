package it.uninsubria.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreferitiDAO {
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
