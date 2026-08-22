package it.uninsubria.dao;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.CoordinateDTO;
import it.uninsubria.dto.LuogoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LuogoDAO {

    public LuogoDTO getLuogoDTO(Connection conn, int idLuogo){
        String sql = "SELECT L.id, L.via, C.id_citta, C.nome AS nome_citta, C.nome_nazione, CO.id AS id_coord, CO.latitudine, CO.longitudine FROM LUOGO L JOIN CITTA C ON L.id_citta = C.id_citta JOIN COORDINATE CO ON L.id_coordinate = CO.id WHERE L.id = ?";
        try(PreparedStatement ps= conn.prepareStatement(sql)){
            ps.setInt(1, idLuogo);
            ResultSet rs=ps.executeQuery();
            if(rs.next())
            {
                CoordinateDTO co = new CoordinateDTO(rs.getInt("id_coord"), rs.getDouble("latitudine"), rs.getDouble("longitudine"));
                CittaDTO ci = new CittaDTO(rs.getInt("id_citta"), rs.getString("nome_citta"), rs.getString("nome_nazione"));
                LuogoDTO lu = new LuogoDTO(rs.getInt("id"), rs.getString("via"), ci, co);
                return lu;
            }

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
