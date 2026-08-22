package it.uninsubria.dao;

import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.RistoratoreDTO;
import it.uninsubria.dto.UtenteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UtenteDAO {

    public UtenteDTO getUtente(Connection conn, int idUtenteRichiesto)
    {
        String sql = "SELECT * FROM UTENTE WHERE id_utente = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtenteRichiesto);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                int idUtente = rs.getInt("id_utente");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String email = rs.getString("email");
                Date dataNascita = rs.getDate("data_nascita");

                LuogoDAO luogoDAO = new LuogoDAO();
                LuogoDTO luogoUtente = luogoDAO.getLuogoDTO(conn, rs.getInt("id_luogo_vive"));
                
                if(rs.getBoolean("is_ristoratore")) {
                    return new RistoratoreDTO(idUtente, nome, cognome, email, dataNascita, luogoUtente);
                } else {
                    return new UtenteDTO(idUtente, nome, cognome, email, dataNascita, luogoUtente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
