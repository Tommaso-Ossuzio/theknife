package it.uninsubria.dao;

import it.uninsubria.dto.FiltroRistoranteDTO;
import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.RistoranteDTO;
import it.uninsubria.dto.RistoratoreDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    /**
     * Ottien il ristorante cercato per nome
     * @param conn
     * @param nomeCercato
     * @return List<RistoranteDTO>
     * @author Elia Toschi
     */
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

    /**
     * Restituisce la lista dei ristoranti preferiti di un utente
     * @param conn
     * @param idUtenteCercato
     * @return List<RistoranteDTO>
     * @author Elia Toschi
     */
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

    /**
     * Restituisce una lista di cucine avendo l'id del ristorante
     * @param conn
     * @param idRistorante
     * @return List<String>
     * @author Elia Toschi
     */
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
     * Funzione ricerca filtro ristoranti
     * @param conn
     * @param filtro
     * @return
     *
     * @author Elia Toschi
     * @author Michele Viselli
     */
    public List<RistoranteDTO> filtraRistoranti(Connection conn, FiltroRistoranteDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException("Il filtro dei ristoranti non può essere null");
        }

        /*
         * Le cucine e le recensioni vengono aggregate prima del collegamento
         * con RISTORANTE. In questo modo non si crea il prodotto cucina x
         * recensioni e le statistiche non vengono conteggiate più volte.
         */
        StringBuilder sql = new StringBuilder(
                "SELECT R.id_ristorante, R.nome, R.telefono, R.sito_web, " +
                        "R.delivery, R.prenotazione_online, R.fascia_prezzo, " +
                        "L.id AS id_luogo, L.via AS via_luogo, " +
                        "C.id_citta, C.nome AS nome_citta, C.nome_nazione, " +
                        "COALESCE(CU.cucine, ARRAY[]::varchar[]) AS cucine, " +
                        "ST.media_stelle, COALESCE(ST.numero_recensioni, 0) AS numero_recensioni " +
                        "FROM RISTORANTE R " +
                        "JOIN LUOGO L ON R.id_luogo = L.id " +
                        "JOIN CITTA C ON L.id_citta = C.id_citta " +
                        "LEFT JOIN (" +
                        "SELECT RTC.id_ristorante, " +
                        "ARRAY_AGG(RTC.nome_tipo_cucina ORDER BY RTC.nome_tipo_cucina) AS cucine " +
                        "FROM RISTORANTE_TIPO_CUCINA RTC " +
                        "GROUP BY RTC.id_ristorante" +
                        ") CU ON CU.id_ristorante = R.id_ristorante " +
                        "LEFT JOIN (" +
                        "SELECT REC.id_ristorante, AVG(REC.numero_stelle) AS media_stelle, " +
                        "COUNT(*) AS numero_recensioni " +
                        "FROM RECENSIONE REC " +
                        "GROUP BY REC.id_ristorante" +
                        ") ST ON ST.id_ristorante = R.id_ristorante " +
                        "WHERE 1=1 ");
        List<Object> parametri = new ArrayList<>();

        if (filtro.getLuogo() != null && !filtro.getLuogo().trim().isEmpty()) {
            sql.append("AND C.nome ILIKE ? ");
            parametri.add("%" + filtro.getLuogo().trim() + "%");
        }

        if (filtro.getCucina() != null && !filtro.getCucina().trim().isEmpty()) {
            sql.append("AND EXISTS (" +
                    "SELECT 1 FROM RISTORANTE_TIPO_CUCINA RTC_FILTRO " +
                    "WHERE RTC_FILTRO.id_ristorante = R.id_ristorante " +
                    "AND RTC_FILTRO.nome_tipo_cucina ILIKE ?" +
                    ") ");
            parametri.add("%" + filtro.getCucina().trim() + "%");
        }

        if (filtro.getFasciaPrezzo() != null && !filtro.getFasciaPrezzo().trim().isEmpty()) {
            sql.append("AND R.fascia_prezzo = ? ");
            parametri.add(filtro.getFasciaPrezzo().trim());
        }

        if (Boolean.TRUE.equals(filtro.getDelivery())) {
            sql.append("AND R.delivery = TRUE ");
        }

        if (Boolean.TRUE.equals(filtro.getPrenotazione())) {
            sql.append("AND R.prenotazione_online = TRUE ");
        }

        if (filtro.getMediaStelleMinima() != null) {
            // Un ristorante senza recensioni ha ST.media_stelle = NULL e non
            // supera correttamente una soglia minima.
            sql.append("AND ST.media_stelle >= ? ");
            parametri.add(filtro.getMediaStelleMinima());
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametri.size(); i++) {
                ps.setObject(i + 1, parametri.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<RistoranteDTO> risultato = new ArrayList<>();
                while (rs.next()) {
                    risultato.add(costruisciRistoranteFiltratoDaResultSet(rs));
                }
                return risultato;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Crea un ristorante dopo aver fatto un filtro
     * @param rs
     * @return
     * @throws SQLException
     * @author Michele Viselli
     */
    private RistoranteDTO costruisciRistoranteFiltratoDaResultSet(ResultSet rs) throws SQLException {
        double media = rs.getDouble("media_stelle");
        Double mediaStelle = rs.wasNull() ? null : media;

        CittaDTO citta = new CittaDTO(
                rs.getInt("id_citta"),
                rs.getString("nome_citta"),
                rs.getString("nome_nazione"));

        LuogoDTO luogo = new LuogoDTO(
                rs.getInt("id_luogo"),
                rs.getString("via_luogo"),
                citta,
                null);

        return new RistoranteDTO(
                rs.getInt("id_ristorante"),
                rs.getString("nome"),
                rs.getString("telefono"),
                rs.getString("sito_web"),
                rs.getBoolean("delivery"),
                rs.getBoolean("prenotazione_online"),
                rs.getString("fascia_prezzo"),
                estraiCucine(rs),
                luogo,
                null,
                mediaStelle,
                rs.getInt("numero_recensioni"),
                null, rs.getInt("stelle_michelin"));
    }

    /**
     * Estrae tutte le cucine da un ResulSet
     * @param rs ResultSet
     * @return List<String>
     * @throws SQLException
     * @author Michele Viselli
     */
    private List<String> estraiCucine(ResultSet rs) throws SQLException {
        Array arrayCucine = rs.getArray("cucine");
        List<String> cucine = new ArrayList<>();

        if (arrayCucine == null) {
            return cucine;
        }

        try {
            Object valori = arrayCucine.getArray();
            if (valori instanceof Object[]) {
                for (Object valore : (Object[]) valori) {
                    if (valore != null) {
                        cucine.add(valore.toString());
                    }
                }
            }
        } finally {
            arrayCucine.free();
        }
        return cucine;
    }


    /**
     * Il metodo crea un ristorante dal ResultSet ottenuto dall'interrogazione del database
     * @param conn
     * @param rs ResultSet
     * @return
     * @throws SQLException
     * @author Elia Toschi
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
        int stelle= rs.getInt("stelle_michelin");

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
                mediaStelle, numeroRecensioni, numeroSenzaRisposta,stelle);
    }

    /**
     * Il metodo calcola le statistiche delle recensioni (media stelle, totale recensioni e recensioni senza risposta
     * @param conn
     * @param idRistorante
     * @return double[] con i valori ordinati in [media, totale, senza_risposta]
     * @author Elia Toschi
     */
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

    /**
     * Estrae tutti i ristoranti di proprietà di un determinato ristoratore.
     * @param conn
     * @param idRistoratore
     * @return Lista di RistoranteDTO
     */
    public List<RistoranteDTO> getRistorantiDelRistoratore(Connection conn, int idRistoratore) {
        String sql = "SELECT * FROM RISTORANTE WHERE id_utente = ?";
        ArrayList<RistoranteDTO> lista = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistoratore);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(costruisciRistoranteDaResultSet(conn, rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserisce un nuovo ristorante nel database (inclusi il luogo e i tipi di cucina).
     * @param conn
     * @param ristorante
     * @return L'ID del ristorante creato, oppure -1 in caso di errore
     * @author Elia Toschi
     * @author Celestino Resteghini
     */
    public void inserisciRistorante(Connection conn, RistoranteDTO ristorante) {
        LuogoDAO luogoDAO = new LuogoDAO();
        int idLuogo = luogoDAO.inserisciLuogoCompleto(conn, ristorante.getLuogo());

        String sql = "INSERT INTO RISTORANTE (nome, telefono, sito_web, delivery, prenotazione_online, fascia_prezzo, id_utente, id_luogo, stelle_michelin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ristorante.getNome());
            ps.setString(2, ristorante.getTelefono());
            ps.setString(3, ristorante.getSitoWeb());
            ps.setBoolean(4, ristorante.isDelivery());
            ps.setBoolean(5, ristorante.isPrenotazioneOnline());
            ps.setString(6, ristorante.getFasciaPrezzo());
            ps.setInt(7, ristorante.getRistoratore().getIdUtente());
            ps.setInt(8, idLuogo);
            ps.setInt(9, ristorante.getStelleMichelin());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int idNuovoRistorante = rs.getInt(1);

                String sqlCucina = "INSERT INTO RISTORANTE_TIPO_CUCINA (id_ristorante, nome_tipo_cucina) VALUES (?, ?)";
                try (PreparedStatement psCucina = conn.prepareStatement(sqlCucina)) {
                    for (String cucina : ristorante.getCucine()) {
                        psCucina.setInt(1, idNuovoRistorante);
                        psCucina.setString(2, cucina);
                        psCucina.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore: Ristorante con questo nome probabilmente già esistente.");
            e.printStackTrace();
        }
    }
}
