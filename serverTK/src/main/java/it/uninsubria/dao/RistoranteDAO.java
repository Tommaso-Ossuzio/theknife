package it.uninsubria.dao;

import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.RistoranteDTO;
import it.uninsubria.dto.RistoratoreDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {


    public List<RistoranteDTO> cercaPerNome(Connection conn, String nomeCercato)
    {
        String sql=" SELECT * FROM RISTORANTE WHERE NOME ILIKE ?";

        try(PreparedStatement ps=conn.prepareStatement(sql))
        {
            ps.setString(1, "%"+nomeCercato+"%");
            ResultSet rs=ps.executeQuery();
            ArrayList<RistoranteDTO> array = new ArrayList<>();

            while(rs.next())
            {
                array.add(costruisciRistoranteDaResultSet(conn, rs));
            }
            return array;

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List<RistoranteDTO> getRistorantiPreferiti(Connection conn, int idUtenteCercato)
    {
        String sql = "SELECT R.* FROM RISTORANTE R JOIN PREFERITO P ON R.id_ristorante = P.id_ristorante WHERE P.id_utente = ?";
        try(PreparedStatement ps =conn.prepareStatement(sql))
        {
            ps.setInt(1, idUtenteCercato);
            ResultSet rs= ps.executeQuery();
            ArrayList<RistoranteDTO> array=new ArrayList<>();
            while (rs.next())
            {
                array.add(costruisciRistoranteDaResultSet(conn, rs));
            }
            return array;
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> getCucinePerRistorante(Connection conn, int idRistorante)
    {
        String sql = "SELECT nome_tipo_cucina FROM RISTORANTE_TIPO_CUCINA WHERE id_ristorante = ?";
        try(PreparedStatement ps= conn.prepareStatement(sql))
        {
            ps.setInt(1, idRistorante);
            ResultSet rs=ps.executeQuery();
            ArrayList<String> array=new ArrayList<>();

            while(rs.next())
            {
                array.add(rs.getString("nome_tipo_cucina"));
            }
            return array;
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Metodo che prende una riga del database e assembla tutto il RistoranteDTO.
     */
    private RistoranteDTO costruisciRistoranteDaResultSet(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id_ristorante");
        String nome = rs.getString("nome");
        String telefono = rs.getString("telefono");
        String sitoWeb = rs.getString("sito_web");
        Boolean delivery = rs.getBoolean("delivery");
        boolean prenotazioneOnline = rs.getBoolean("prenotazione_online");
        String fasciaPrezzo = rs.getString("fascia_prezzo");

        int idLuogo = rs.getInt("id_luogo");
        int idUtente = rs.getInt("id_utente");
        
        List<String> lista = getCucinePerRistorante(conn, id);
        
        LuogoDAO luogoDAO = new LuogoDAO();
        LuogoDTO luogo = luogoDAO.getLuogoDTO(conn, idLuogo);
        
        UtenteDAO utenteDAO = new UtenteDAO();
        RistoratoreDTO proprietario = (RistoratoreDTO) utenteDAO.getUtente(conn, idUtente);

        return new RistoranteDTO(id, nome, telefono, sitoWeb, delivery, prenotazioneOnline,
                fasciaPrezzo, lista, luogo, proprietario, null, null, null);
    }
}
