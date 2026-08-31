/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
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

/**
 * L'utenteDAO rappresenta il punto di accesso al database per le informazioni degli utenti
 * @author Elia Toschi
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 */
public class UtenteDAO {

    /**
     * Registra l'utente nel db e crea le tabelle necessarie
     * @param conn
     * @param utente
     * @param passwordHash
     * @return false se la mail è già in uso, true se registrato
     * @author Celestino Resteghini
     * @author Elia Toschi
     */
    public boolean registraUtente(Connection conn, UtenteDTO utente, String passwordHash) {
        if (!UtenteDTO.emailValida(utente.getEmail())) {
            System.err.println("Registrazione bloccata: Email non valida!");
            return false;
        }

        if (!UtenteDTO.dataNascitaValida(utente.getDataNascita())) {
            System.err.println("Registrazione bloccata: Data di nascita non valida!");
            return false;
        }

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
        int idLuogo = luogoDAO.getIdLuogoByCitta(conn, utente.getLuogo().getCitta().getNome());

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
            if(utente.getDataNascita() == null)
                psInsert.setDate(5, null);
            else
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
     * @param conn connessione al db
     * @param credenziali AuthDTO
     * @return true se successo o false se credenziali errate
     * @author Elia Toschi
     */
    public boolean eseguiLogin(Connection conn, AuthDTO credenziali) {

        String sql = "SELECT id_utente FROM UTENTE WHERE email = ? AND password = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, credenziali.getEmail());
            ps.setString(2, credenziali.getPassword());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                if(rs.getInt("id_utente")>0)
                    return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.err.println("Login fallito: Credenziali errate.");
        return false;
    }

    /**
     * Metodoto che controlla se un utente è ristoratore
     * @param conn
     * @param email
     * @return true se è ristoratore o false se non lo è oppure credenziali errate
     * @author Celestino Resteghini
     */
    public boolean isRistoratore(Connection conn, String email) {

        String sql = "SELECT is_ristoratore FROM UTENTE WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_ristoratore");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.err.println("L'utente non trovato.");
        return false;
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

    /**
     * Restituisce l'idUtente sapendo la mail
     * @param conn connesione al database
     * @param email email univoca dell'utente
     * @return int id dell'utente
     * @author Elia Toschi
     */
    public int getIdUtenteDaMail(Connection conn, String email)
    {
        String sql = "SELECT id_utente FROM UTENTE WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_utente");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore id utente non trovato");
            e.printStackTrace();
        }

        return -1;
    }
}
