package it.uninsubria.dao;

import it.uninsubria.dto.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * RecensioneDAO rappresenta il punto di accesso al database delle informazioni delle recensioni e risposte
 * @author Elia Toschi
 */
public class RecensioneDAO {
    /**
     * Estrae tutte le recensioni relative a un ristorante specifico.
     * @param conn
     * @param idRistorante
     * @return Lista di recensioniDTO
     * @author Elia Toschi
     */
    public List<RecensioneDTO> getRecensioniPerRistorante(Connection conn, int idRistorante) {
        String sql = "SELECT * FROM RECENSIONE WHERE id_ristorante = ? ORDER BY data_ora DESC";
        LinkedList<RecensioneDTO> listaRecensioni = new LinkedList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ResultSet rs = ps.executeQuery();

            DateTimeFormatter formattatoreData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formattatoreOra = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                int idRecensione = rs.getInt("id");
                String testo = rs.getString("testo");
                int numeroStelle = rs.getInt("numero_stelle");
                int idAutore = rs.getInt("id_utente");
                int idRistoranteRecensione = rs.getInt("id_ristorante");

                Timestamp ts = rs.getTimestamp("data_ora");
                LocalDateTime ldt = ts.toLocalDateTime();
                String data = ldt.format(formattatoreData);
                String ora = ldt.format(formattatoreOra);

                RispostaDTO risposta = getRispostaPerRecensione(conn, idRecensione);

                RecensioneDTO recensione = new RecensioneDTO(
                        idRistoranteRecensione,
                        testo,
                        numeroStelle,
                        data,
                        ora,
                        idAutore,
                        risposta,
                        idRecensione
                );
                listaRecensioni.add(recensione);
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return listaRecensioni;
    }

    /**
     * Metodo aiutante: Estrae un'eventuale risposta alla recensione, se esiste.
     * @param conn
     * @param idRecensione
     * @return RispostaDTO
     * @author Elia Toschi
     */
    private RispostaDTO getRispostaPerRecensione(Connection conn, int idRecensione) {
        String sql = "SELECT * FROM RISPOSTA WHERE id_recensione = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRecensione);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String testoRisposta = rs.getString("testo_risposta");
                int idRistoratore = rs.getInt("id_utente");

                UtenteDAO utenteDAO = new UtenteDAO();
                UtenteDTO utenteDTO =  utenteDAO.getUtente(conn, idRistoratore);
                RistoratoreDTO ristoratore = new RistoratoreDTO(utenteDTO.getIdUtente(),utenteDTO.getNome(), utenteDTO.getCognome(),utenteDTO.getEmail(), utenteDTO.getDataNascita(), utenteDTO.getLuogo());

                return new RispostaDTO(testoRisposta, idRecensione, ristoratore);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Inserisce una recensione
     * @param conn
     * @param recensione RecensioneDTO
     * @author Elia Toschi
     */
    public void inserisciRecensione(Connection conn, RecensioneDTO recensione) {
        String sql = "INSERT INTO RECENSIONE (data_ora, testo, numero_stelle, id_utente, id_ristorante) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setString(2, recensione.getTesto());
            ps.setInt(3, recensione.getNumeroStelle());
            ps.setInt(4, recensione.getIdUtente());
            ps.setInt(5, recensione.getIdRistorante());

            ps.executeUpdate();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserisce nel db la risposta
     * @param conn
     * @param risposta
     * @author Elia Toschi
     */
    public void inserisciRisposta(Connection conn, RispostaDTO risposta) {
        String sql = "INSERT INTO RISPOSTA (id_recensione, id_utente, testo_risposta) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, risposta.getIdRecensione());
            ps.setInt(2, risposta.getRistoratore().getIdUtente());
            ps.setString(3, risposta.getTesto());

            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifica il testo e le stelle di una recensione esistente.
     * @param conn
     * @param recensione RecenisoneDTO
     * @author Elia Toschi
     */
    public void modificaRecensione(Connection conn, RecensioneDTO recensione) {
        String sql = "UPDATE RECENSIONE SET testo = ?, numero_stelle = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recensione.getTesto());
            ps.setInt(2, recensione.getNumeroStelle());

            ps.setInt(3, recensione.getIdRecensione());

            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Estrae tutte le recensioni scritte da un determinato utente.
     * @param conn
     * @param idUtente
     * @return Lista di RecensioneDTO
     * @author Elia Toschi
    */
    public List<RecensioneDTO> getRecensioniDaUtente(Connection conn, int idUtente) {
        String sql = """
                SELECT REC.*, RIST.nome AS nome_ristorante
                FROM RECENSIONE REC
                JOIN RISTORANTE RIST
                    ON RIST.id_ristorante = REC.id_ristorante
                WHERE REC.id_utente = ?
                ORDER BY REC.data_ora DESC
                """;
        LinkedList<RecensioneDTO> listaRecensioni = new LinkedList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtente);
            ResultSet rs = ps.executeQuery();

            DateTimeFormatter formattatoreData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formattatoreOra = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                int idRecensione = rs.getInt("id");
                String testo = rs.getString("testo");
                int numeroStelle = rs.getInt("numero_stelle");
                int idRistorante = rs.getInt("id_ristorante");

                Timestamp ts = rs.getTimestamp("data_ora");
                LocalDateTime ldt = ts.toLocalDateTime();
                String data = ldt.format(formattatoreData);
                String ora = ldt.format(formattatoreOra);

                RispostaDTO risposta = getRispostaPerRecensione(conn, idRecensione);

                RecensioneDTO recensione = new RecensioneDTO(idRistorante, testo, numeroStelle, data, ora, idUtente, risposta, idRecensione);
                recensione.setNomeRistorante(rs.getString("nome_ristorante"));
                listaRecensioni.add(recensione);
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return listaRecensioni;
    }
    /**
     * Trova tutte le recensioni relative ai ristoranti di un ristoratore che non hanno risposta
     * @param conn
     * @param idRistoratore
     * @return Lista di RecensioneDTO (senza risposta)
     * @author Elia Toschi
     */
    public List<RecensioneDTO> getRecensioniSenzaRisposta(Connection conn, int idRistoratore) {
        String sql = "SELECT REC.* " +
                "FROM RECENSIONE REC " +
                "JOIN RISTORANTE RIST ON REC.id_ristorante = RIST.id_ristorante " +
                "LEFT JOIN RISPOSTA RISP ON REC.id = RISP.id_recensione " +
                "WHERE RIST.id_utente = ? AND RISP.id_recensione IS NULL " +
                "ORDER BY REC.data_ora DESC";

        LinkedList<RecensioneDTO> lista = new LinkedList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistoratore);
            ResultSet rs = ps.executeQuery();

            DateTimeFormatter formattatoreData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formattatoreOra = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                int idRecensione = rs.getInt("id");
                String testo = rs.getString("testo");
                int numeroStelle = rs.getInt("numero_stelle");
                int idAutore = rs.getInt("id_utente");
                int idRistorante = rs.getInt("id_ristorante");

                Timestamp ts = rs.getTimestamp("data_ora");
                LocalDateTime ldt = ts.toLocalDateTime();
                String data = ldt.format(formattatoreData);
                String ora = ldt.format(formattatoreOra);


                RispostaDTO risposta = null;

                RecensioneDTO recensione = new RecensioneDTO(testo, numeroStelle, data, ora, idAutore, idRistorante, risposta);

                recensione.setIdRecensione(idRecensione);

                lista.add(recensione);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Elimina la recensione da un id
     * @param conn
     * @param idRecensione
     * @author Elia Toschi
     */
    public void eliminaRecensione(Connection conn, int idRecensione) {
        String sql = "DELETE FROM RECENSIONE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRecensione);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
