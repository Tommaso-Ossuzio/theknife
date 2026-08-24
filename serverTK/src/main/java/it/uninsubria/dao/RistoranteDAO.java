package it.uninsubria.dao;

import it.uninsubria.dto.FiltroRistoranteDTO;
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
    public List<RistoranteDTO> filtraRistoranti(Connection conn, FiltroRistoranteDTO filtro) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT R.* FROM RISTORANTE R ");
        List<Object> parametri = new ArrayList<>();

        if (filtro.getLuogo() != null && !filtro.getLuogo().trim().isEmpty()) {
            sql.append("JOIN LUOGO L ON R.id_luogo = L.id ").append("JOIN CITTA C ON L.id_citta = C.id_citta ");
        }
        if (filtro.getCucina() != null && !filtro.getCucina().trim().isEmpty()) {
            sql.append("JOIN RISTORANTE_TIPO_CUCINA RTC ON R.id_ristorante = RTC.id_ristorante ");
        }
        if (filtro.getMediaStelleMinima() != null) {
            sql.append("LEFT JOIN RECENSIONE REC ON R.id_ristorante = REC.id_ristorante ");
        }

        // WHERE
        sql.append("WHERE 1=1 ");
        if (filtro.getLuogo() != null && !filtro.getLuogo().trim().isEmpty()) {
            sql.append("AND C.nome ILIKE ? ");
            parametri.add("%" + filtro.getLuogo().trim() + "%");
        }
        if (filtro.getCucina() != null && !filtro.getCucina().trim().isEmpty()) {
            sql.append("AND RTC.nome_tipo_cucina ILIKE ? ");
            parametri.add("%" + filtro.getCucina().trim() + "%");
        }
        if (filtro.getFasciaPrezzo() != null && !filtro.getFasciaPrezzo().trim().isEmpty()) {
            sql.append("AND R.fascia_prezzo = ? ");
            parametri.add(filtro.getFasciaPrezzo().trim());
        }
        if (filtro.getDelivery() != null && filtro.getDelivery()) {
            sql.append("AND R.delivery = true ");
        }
        if (filtro.getPrenotazione() != null && filtro.getPrenotazione()) {
            sql.append("AND R.prenotazione_online = true ");
        }
        // RAGGRUPPAMENTO E MEDIA STELLE
        if (filtro.getMediaStelleMinima() != null) {
            sql.append("GROUP BY R.id_ristorante, R.nome, R.telefono, R.sito_web, R.delivery, ")
                    .append("R.prenotazione_online, R.fascia_prezzo, R.id_luogo, R.id_utente ")
                    .append("HAVING AVG(REC.numero_stelle) >= ? ");
            parametri.add(filtro.getMediaStelleMinima());
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametri.size(); i++) {
                ps.setObject(i + 1, parametri.get(i));
            }
            ResultSet rs = ps.executeQuery();
            List<RistoranteDTO> risultato = new ArrayList<>();

            while (rs.next()) {
                risultato.add(costruisciRistoranteDaResultSet(conn, rs));
            }
            return risultato;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


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

        double[] statistiche = calcolaStatisticheRecensioni(conn, id);
        Double mediaStelle = statistiche[0] > 0 ? statistiche[0] : 0.0;
        Integer numeroRecensioni = (int) statistiche[1];
        Integer numeroSenzaRisposta = (int) statistiche[2];

        return new RistoranteDTO(id, nome, telefono, sitoWeb, delivery, prenotazioneOnline,
                fasciaPrezzo, lista, luogo, proprietario,
                mediaStelle, numeroRecensioni, numeroSenzaRisposta);
    }

    private double[] calcolaStatisticheRecensioni(Connection conn, int idRistorante) {
        // Valori di default a 0.0 se non ci sono recensioni
        double[] risultati = new double[3];

        String sql = "SELECT " +
                "  AVG(REC.numero_stelle) AS media, " +
                "  COUNT(REC.id) AS totale, " +
                "  SUM(CASE WHEN RISP.id_recensione IS NULL THEN 1 ELSE 0 END) AS senza_risposta " +
                "FROM RECENSIONE REC " +
                "LEFT JOIN RISPOSTA RISP ON REC.id = RISP.id_recensione " +
                "WHERE REC.id_ristorante = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                risultati[0] = rs.getDouble("media");
                risultati[1] = rs.getInt("totale");
                risultati[2] = rs.getInt("senza_risposta");
            }
        } catch (SQLException e) {
            System.err.println("Errore nel calcolo statistiche recensioni per Ristorante " + idRistorante);
            e.printStackTrace();
        }

        return risultati;
    }
}
