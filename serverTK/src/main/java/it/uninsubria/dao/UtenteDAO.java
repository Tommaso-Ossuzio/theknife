package it.uninsubria.dao;

import it.uninsubria.dto.AuthDTO;
import it.uninsubria.dto.LuogoDTO;
import it.uninsubria.dto.RistoratoreDTO;
import it.uninsubria.dto.UtenteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UtenteDAO {


    /**
     *
     * Registra l'utente nel db e crea le tabelle necessarie
     * @param conn
     * @param utente
     * @param passwordHash
     * @return false se la mail è già in uso, true se registrato
     */
    public boolean registraUtente(Connection conn, UtenteDTO utente, String passwordHash) {

        String checkEmailSql = "SELECT id_utente FROM UTENTE WHERE email = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkEmailSql)) {
            psCheck.setString(1, utente.getEmail());
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                System.err.println("Registrazione bloccata: Email già in uso!");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        LuogoDAO luogoDAO = new LuogoDAO();
        int idLuogo = luogoDAO.inserisciLuogoCompleto(conn, utente.getLuogo());

        if (idLuogo == -1) {
            return false;
        }

        boolean isRistoratore = (utente instanceof RistoratoreDTO);
        String insertSql = "INSERT INTO UTENTE (email, nome, cognome, password, data_nascita, is_ristoratore, id_luogo_vive) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            psInsert.setString(1, utente.getEmail());
            psInsert.setString(2, utente.getNome());
            psInsert.setString(3, utente.getCognome());
            psInsert.setString(4, passwordHash);
            psInsert.setDate(5, new java.sql.Date(utente.getDataNascita().getTime()));
            psInsert.setBoolean(6, isRistoratore);
            psInsert.setInt(7, idLuogo);

            psInsert.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Accede al db e controlla le credenziali
     * @param conn
     * @param credenziali AuthDTO
     * @return idUtente se successo o null se credenziali errato
     * @author Elia Toschi
     */
    public UtenteDTO eseguiLogin(Connection conn, AuthDTO credenziali) {

        String sql = "SELECT id_utente FROM UTENTE WHERE email = ? AND password = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, credenziali.getEmail());
            ps.setString(2, credenziali.getPassword());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int idTrovato = rs.getInt("id_utente");
                return this.getUtente(conn, idTrovato);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.err.println("Login fallito: Credenziali errate.");
        return null;
    }

    /**
     * Restituisce l'utente passando l'id
     * @param conn
     * @param idUtenteRichiesto
     * @return UtenteDTO
     * @author Elia Toschi
     */
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
