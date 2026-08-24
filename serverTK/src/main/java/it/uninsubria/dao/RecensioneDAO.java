package it.uninsubria.dao;

import it.uninsubria.dto.RecensioneDTO;
import it.uninsubria.dto.RispostaDTO;
import it.uninsubria.dto.RistoratoreDTO;
import it.uninsubria.dto.UtenteDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RecensioneDAO {
    /**
     * Estrae tutte le recensioni relative a un ristorante specifico.
     */
    public List<RecensioneDTO> getRecensioniPerRistorante(Connection conn, int idRistorante) {
        String sql = "SELECT * FROM RECENSIONE WHERE id_ristorante = ? ORDER BY data_ora DESC";
        ArrayList<RecensioneDTO> listaRecensioni = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ResultSet rs = ps.executeQuery();

            // Prepariamo i DAO e i formattatori
            UtenteDAO utenteDAO = new UtenteDAO();
            DateTimeFormatter formattatoreData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formattatoreOra = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                int idRecensione = rs.getInt("id");
                String testo = rs.getString("testo");
                int numeroStelle = rs.getInt("numero_stelle");
                int idAutore = rs.getInt("id_utente");

                Timestamp ts = rs.getTimestamp("data_ora");
                LocalDateTime ldt = ts.toLocalDateTime();
                String data = ldt.format(formattatoreData);
                String ora = ldt.format(formattatoreOra);

                RispostaDTO risposta = getRispostaPerRecensione(conn, idRecensione);

                RecensioneDTO recensione = new RecensioneDTO( testo, numeroStelle, data, ora, idAutore, risposta);
                listaRecensioni.add(recensione);
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return listaRecensioni;
    }

    /**
     * Metodo aiutante: Estrae un'eventuale risposta alla recensione, se esiste.
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
                RistoratoreDTO ristoratore = (RistoratoreDTO) utenteDAO.getUtente(conn, idRistoratore);

                return new RispostaDTO(testoRisposta, ristoratore);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
