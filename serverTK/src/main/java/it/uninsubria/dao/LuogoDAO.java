package it.uninsubria.dao;

import it.uninsubria.dto.CittaDTO;
import it.uninsubria.dto.CoordinateDTO;
import it.uninsubria.dto.LuogoDTO;

import java.sql.*;

/**
 * interfaccia al database per le informazioni del luogo
 * @author Elia Toschi
 */
public class LuogoDAO {

    /**
     * Restituisce il luogo dall'id
     * @param conn
     * @param idLuogo
     * @return LuogoDTO
     * @author Elia Toschi
     */
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

    /**
     * Inserisce le coordinate nel db
     * @param conn
     * @param coord
     * @return l'id delle coordinate, -1 se da errore
     * @author Elia Toschi
     */
    public int inserisciCoordinate(Connection conn, CoordinateDTO coord) {
        String sql = "INSERT INTO COORDINATE (latitudine, longitudine) VALUES (?, ?)";

        // prende l'id genrato
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, coord.getLatitudine());
            ps.setDouble(2, coord.getLongitudine());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Inserisce la città e nazione se non esistono
     * @param conn
     * @param citta
     * @return l'id o -1 se da errore
     * @author Elia Toschi
     */
    public int inserisciCitta(Connection conn, CittaDTO citta) {
        // Verifichiamo che la nazione esista
        String sqlNazione = "INSERT INTO NAZIONE (nome) VALUES (?) ON CONFLICT DO NOTHING";
        try (PreparedStatement psN = conn.prepareStatement(sqlNazione)) {
            psN.setString(1, citta.getNazione());
            psN.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Verifichiamo se esiste la città
        String sqlCerca = "SELECT id_citta FROM CITTA WHERE nome = ? AND nome_nazione = ?";
        try (PreparedStatement psCerca = conn.prepareStatement(sqlCerca)) {
            psCerca.setString(1, citta.getNome());
            psCerca.setString(2, citta.getNazione());
            ResultSet rs = psCerca.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_citta");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Inseriamo la città
        String sqlInsert = "INSERT INTO CITTA (nome, nome_nazione) VALUES (?, ?)";
        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            psInsert.setString(1, citta.getNome());
            psInsert.setString(2, citta.getNazione());
            psInsert.executeUpdate();

            ResultSet rs = psInsert.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Inserisce il luogo
     * @param conn
     * @param luogo
     * @return id, -1 se da errore
     * @author Elia Toschi
     */
    public int inserisciLuogoCompleto(Connection conn, LuogoDTO luogo) {
        //Creaiamo città e coordinate
        int idCitta = inserisciCitta(conn, luogo.getCitta());
        int idCoordinate = inserisciCoordinate(conn, luogo.getCoordinate());

        // inseriamo il luogo
        String sql = "INSERT INTO LUOGO (via, id_citta, id_coordinate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, luogo.getVia());
            ps.setInt(2, idCitta);
            ps.setInt(3, idCoordinate);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Estrae le coordinate di un ristorante partendo dal suo ID.
     * @param conn
     * @param idRistorante
     * @return CoordinateDTO o null se non trovato
     * @author Elia Toschi
     */
    public CoordinateDTO getCoordinateRistorante(Connection conn, int idRistorante) {
        String sql = "SELECT C.id, C.latitudine, C.longitudine " +
                "FROM COORDINATE C " +
                "JOIN LUOGO L ON C.id = L.id_coordinate " +
                "JOIN RISTORANTE R ON L.id = R.id_luogo " +
                "WHERE R.id_ristorante = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new CoordinateDTO(
                        rs.getInt("id"),
                        rs.getDouble("latitudine"),
                        rs.getDouble("longitudine")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Metodo per ottenere l'id di un luogo conoscendo il nome della città
     * @param conn
     * @param nomeCitta
     * @author Celestino Resteghini
     */
    public int getIdLuogoByCitta(Connection conn, String nomeCitta) {
        String sql = "SELECT L.id " +
                "FROM LUOGO L " +
                "JOIN CITTA C ON L.id_citta = C.id_citta " +
                "WHERE LOWER(C.nome) = LOWER(?) " +
                "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeCitta.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore id del luogo per la città: " + nomeCitta + " non trovato");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Metodo per ottenere il dimicilio di un utente
     * @author Celestino Resteghini
     * @param conn
     * @param email
     * @return nome della città del domicilio
     */
    public String getDomicilio(Connection conn, String email) {
        String sql = "SELECT C.nome " +
                "FROM UTENTE U " +
                "JOIN LUOGO L ON U.id_luogo_vive = L.id " +
                "JOIN CITTA C ON L.id_citta = C.id_citta " +
                "WHERE LOWER(U.email) = LOWER(?) " +
                "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nome della citta per email: " + email + " non trovato");
            e.printStackTrace();
        }

        return "ERRORE";
    }
}
