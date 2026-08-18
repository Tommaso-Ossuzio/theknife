package it.uninsubria;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ImportatoreDati {

    /**
     * Metodo principale per l'importazione.
     */
    public static void importa(Connection conn) {
        // Usa il path relativo partendo dalla root del progetto IDEA
        String csvPath = "data/michelin_my_maps.csv";
        
        System.out.println("\n--- Inizio importazione dati dal CSV Michelin ---");

        //verifico l'esistenza dell'utente
        int idAdmin = getOrCreateAdminUser(conn);
        if (idAdmin == -1) {
            System.err.println("Impossibile creare l'utente Admin. Importazione annullata.");
            return;
        }

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] header = reader.readNext(); // Salta l'intestazione delle colonne
            if (header == null) return;

            String[] line;
            int count = 0;
            
            // Ciclo riga per riga
            while ((line = reader.readNext()) != null) { 
                // ignora le colonne che non ci servono
                if (line.length < 7) continue;

                String nomeRist = line[0].trim();
                String via = line[1].trim();
                String location = line[2].trim();
                String fasciaPrezzo = line[3].trim();
                String cucineStr = line[4].trim();
                
                double lon, lat;
                try {
                    lon = Double.parseDouble(line[5]);
                    lat = Double.parseDouble(line[6]);
                } catch (NumberFormatException e) {
                    continue; // Salta il record se le coordinate sono corrotte o assenti
                }

                // Limitiamo la lunghezza dei campi per rispettare i VARCHAR(100) del database
                if (nomeRist.length() > 100)
                    nomeRist = nomeRist.substring(0, 100);
                if (fasciaPrezzo.length() > 50)
                    fasciaPrezzo = fasciaPrezzo.substring(0, 50);

                // Divide nazione e città
                String[] locParts = location.split(", ");
                String nomeCitta = locParts[0].trim();
                if (nomeCitta.length() > 100) nomeCitta = nomeCitta.substring(0, 100);
                
                String nomeNazione = locParts.length > 1 ? locParts[1].trim() : "Sconosciuta";
                if (nomeNazione.length() > 100) nomeNazione = nomeNazione.substring(0, 100);

                try {
                    inserisciNazione(conn, nomeNazione);
                    int idCitta = inserisciCitta(conn, nomeCitta, nomeNazione);
                    int idCoord = inserisciCoordinate(conn, lat, lon);
                    int idLuogo = inserisciLuogo(conn, via, idCitta, idCoord);
                    
                    inserisciRistorante(conn, nomeRist, fasciaPrezzo, idAdmin, idLuogo);

                    String[] cucine = cucineStr.split(", ");
                    for (String c : cucine) {
                        String tipo = c.trim();
                        if (!tipo.isEmpty()) {
                            if (tipo.length() > 100) tipo = tipo.substring(0, 100);
                            inserisciTipoCucina(conn, tipo);
                            collegaRistoranteCucina(conn, nomeRist, tipo);
                        }
                    }
                    
                    count++;
                    if (count % 100 == 0) {
                        System.out.println("Importati " + count + " ristoranti...");
                    }
                    
                } catch (SQLException e) {
                    // Se una singola riga fallisce (es. duplicato), andiamo avanti con la prossima
                    // System.err.println("Errore inserimento ristorante: " + nomeRist + " - " + e.getMessage());
                }
            }
            System.out.println("Importazione completata con successo! Ristoranti totali inseriti: " + count);
            
        } catch (IOException | CsvValidationException e) {
            System.err.println("Impossibile leggere il file CSV: " + e.getMessage());
        }
    }

    // --- METODI DI SUPPORTO JDBC ---

    private static int getOrCreateAdminUser(Connection conn) {
        String email = "admin@michelin.com";
        // Controlla se esiste
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_utente FROM UTENTE WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_utente");
        } catch (SQLException e) { e.printStackTrace(); }

        try {
            inserisciNazione(conn, "Italia");
            int idCitta = inserisciCitta(conn, "Varese", "Italia");
            int idCoord = inserisciCoordinate(conn, 45.7772, 3.0870);
            int idLuogo = inserisciLuogo(conn, "Monte Generoso", idCitta, idCoord);

            String sql = "INSERT INTO UTENTE (email, nome, cognome, password, is_ristoratore, id_luogo_vive) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, email);
                ps.setString(2, "Guida");
                ps.setString(3, "Michelin");
                ps.setString(4, "michelin123"); 
                ps.setBoolean(5, true); // DEVE essere ristoratore per possedere ristoranti
                ps.setInt(6, idLuogo);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private static void inserisciNazione(Connection conn, String nome) throws SQLException {
        String sql = "INSERT INTO NAZIONE (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
        }
    }

    private static int inserisciCitta(Connection conn, String nome, String nazione) throws SQLException {
        String check = "SELECT id_citta FROM CITTA WHERE nome = ? AND nome_nazione = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        
        String sql = "INSERT INTO CITTA (nome, nome_nazione) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Citta: " + nome);
    }

    private static int inserisciCoordinate(Connection conn, double lat, double lon) throws SQLException {
        String sql = "INSERT INTO COORDINATE (latitudine, longitudine) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Coordinate");
    }

    private static int inserisciLuogo(Connection conn, String via, int idCitta, int idCoord) throws SQLException {
        String sql = "INSERT INTO LUOGO (via, id_citta, id_coordinate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (via.length() > 255) via = via.substring(0, 255);
            ps.setString(1, via);
            ps.setInt(2, idCitta);
            ps.setInt(3, idCoord);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Luogo");
    }

    private static void inserisciRistorante(Connection conn, String nome, String prezzo, int idUtente, int idLuogo) throws SQLException {
        String sql = "INSERT INTO RISTORANTE (nome, delivery, prenotazione_online, fascia_prezzo, id_utente, id_luogo) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setBoolean(2, false);
            ps.setBoolean(3, false);
            ps.setString(4, prezzo);
            ps.setInt(5, idUtente);
            ps.setInt(6, idLuogo);
            ps.executeUpdate();
        }
    }

    private static void inserisciTipoCucina(Connection conn, String nome) throws SQLException {
        String sql = "INSERT INTO TIPO_CUCINA (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
        }
    }

    private static void collegaRistoranteCucina(Connection conn, String nomeRist, String nomeCucina) throws SQLException {
        String sql = "INSERT INTO RISTORANTE_TIPO_CUCINA (nome_ristorante, nome_tipo_cucina) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeRist);
            ps.setString(2, nomeCucina);
            ps.executeUpdate();
        }
    }
}
