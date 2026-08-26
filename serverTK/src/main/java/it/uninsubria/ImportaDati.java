package it.uninsubria;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.*;

/**
 * La classe permette di importare tutti i file csv nel database
 * @author Elia Toschi
 */
public class ImportaDati {

    /**
     * Legge il file CSV dei ristoranti e popola il database.Legge i campi in modo sicuro.
     * importa le relazioni necessarie (Città, Nazioni, Coordinate, Tipi Cucina).
     * @param conn La connessione al database
     * @author Elia Toschi
     */
    public static void importa(Connection conn) {
        // Legge il CSV dalle risorse (funziona sia su IntelliJ che dentro il .jar!)
        InputStream is = ImportaDati.class.getResourceAsStream("/db/migration/michelin_my_maps.csv");
        if (is == null) {
            System.err.println("File CSV non trovato in resources/db/migration/michelin_my_maps.csv!");
            return;
        }

        // --- CONTROLLO: Il DB è già popolato? ---
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM RISTORANTE")) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Il database contiene già " + rs.getInt(1) + " ristoranti. L'importazione CSV verrà saltata per risparmiare tempo.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Errore nel controllo del DB: " + e.getMessage());
        }

        System.out.println("\n--- Inizio importazione dati dal CSV Michelin ---");

        //verifico l'esistenza dell'utente
        int idAdmin = getOrCreateAdminUser(conn);
        if (idAdmin == -1) {
            System.err.println("Impossibile creare l'utente Admin. Importazione annullata.");
            return;
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
            String[] header = reader.readNext(); // Salta l'intestazione delle colonne
            if (header == null)
                return;

            String[] line;
            int count = 0;
            Utility utility = new Utility();

            while ((line = reader.readNext()) != null) {
                if (line.length < 8)
                    continue;

                String nomeRist = line[0].trim();
                String via = line[1].trim();
                String location = line[2].trim();
                String fasciaPrezzo = line[3].trim();
                String cucineStr = line[4].trim();

                String awardStr= line[10].trim();
                int stelle=0;
                if(awardStr.contains("3 Stars")){
                    stelle=3;
                }else if(awardStr.contains("2 Stars")){
                    stelle=2;
                }else if(awardStr.contains("1 Star")) {
                    stelle=1;
                }

                boolean delivery = false;
                if (line.length > 14 && line[14] != null && !line[14].trim().equalsIgnoreCase("NULL")) {
                    String delStr = line[14].trim().toLowerCase();
                    delivery = delStr.equals("1") || delStr.equals("true");
                }
                boolean prenotazione_online = false;
                if (line.length > 15 && line[15] != null && !line[15].trim().equalsIgnoreCase("NULL")) {
                    String bookStr = line[15].trim().toLowerCase();
                    prenotazione_online = bookStr.equals("1") || bookStr.equals("true");
                }
                // Conversione simboli prezzo (es. €€ -> "tra 35 € e 60 €")
                fasciaPrezzo = utility.ConvertiStringaPrezzo(fasciaPrezzo);
                fasciaPrezzo= (fasciaPrezzo);

                String telefono = line[7].trim();
                String sitoWeb = (line.length > 9) ? line[9].trim() : null;

                // Pulisci stringhe vuote
                if (sitoWeb != null && (sitoWeb.isEmpty() || sitoWeb.equalsIgnoreCase("NULL")))
                    sitoWeb = null;
                if (telefono != null && (telefono.isEmpty() || telefono.equalsIgnoreCase("NULL")))
                    telefono = null;

                double lon, lat;
                try {
                    lon = Double.parseDouble(line[5]);
                    lat = Double.parseDouble(line[6]);
                } catch (NumberFormatException e) {
                    continue; // Salta il record se le coordinate sono corrotte o assenti
                }

                // Limitiamo la lunghezza dei campi per rispettare i VARCHAR(100) del database
                if (nomeRist.length() > 100) nomeRist = nomeRist.substring(0, 100);
                if (fasciaPrezzo.length() > 50) fasciaPrezzo = fasciaPrezzo.substring(0, 50);
                if (telefono != null && telefono.length() > 50)
                    telefono = telefono.substring(0, 50);
                if (sitoWeb != null && sitoWeb.length() > 255)
                    sitoWeb = sitoWeb.substring(0, 255);

                // Divide nazione e città
                String[] locParts = location.split(", ");
                String nomeCitta = locParts[0].trim();
                if (nomeCitta.length() > 100) nomeCitta = nomeCitta.substring(0, 100);

                String nomeNazione = locParts.length > 1 ? locParts[1].trim() : "Sconosciuta";
                if (nomeNazione.length() > 100)
                    nomeNazione = nomeNazione.substring(0, 100);

                try {
                    inserisciNazione(conn, nomeNazione);
                    int idCitta = inserisciCitta(conn, nomeCitta, nomeNazione);
                    int idCoord = inserisciCoordinate(conn, lat, lon);
                    int idLuogo = inserisciLuogo(conn, via, idCitta, idCoord);
                    int idRistorante = inserisciRistorante(conn, nomeRist, telefono, sitoWeb, fasciaPrezzo,  idAdmin, idLuogo, stelle,delivery,prenotazione_online);

                    String[] cucine = cucineStr.split(", ");
                    for (String c : cucine) {
                        String tipo = c.trim();
                        if (!tipo.isEmpty()) {
                            if (tipo.length() > 100)
                                tipo = tipo.substring(0, 100);
                            inserisciTipoCucina(conn, tipo);
                            collegaRistoranteCucina(conn, idRistorante, tipo);
                        }
                    }

                    count++;
                    if (count % 100 == 0) {
                        System.out.println("Importati " + count + " ristoranti...");
                    }

                } catch (SQLException e) {
                    // Ignora in caso di duplicati o errori minori
                }
            }
            System.out.println("Importazione completata con successo! Ristoranti totali inseriti: " + count);

        } catch (IOException | CsvValidationException e) {
            System.err.println("Impossibile leggere il file CSV: " + e.getMessage());
        }
        importaUtenti(conn);
        importaRecensioni(conn);
    }

    // --- METODI DI SUPPORTO JDBC ---
    /**
     * Crea un utente admin per assegnargli i ristoranti predefiniti
     * @param conn La connessione al database
     * @return L'ID dell'utente amministratore, oppure -1 in caso di errore
     * @author Elia Toschi
     */
    private static int getOrCreateAdminUser(Connection conn) {
        String email = "admin@michelin.com";
        // Controlla se esiste
        try (PreparedStatement ps = conn.prepareStatement("SELECT id_utente FROM UTENTE WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("id_utente");
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
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * Inserisce una nuova nazione nel database. Non la inserice se presente
     * @param conn connessione al database
     * @param nome Il nome della nazione da inserire
     * @throws SQLException errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static void inserisciNazione(Connection conn, String nome) throws SQLException {
        String sql = "INSERT INTO NAZIONE (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
        }
    }

    /**
     * Inserisce una nuova città nel database associandola alla sua nazione.
     * @param conn La connessione al database
     * @param nome Il nome della città
     * @param nazione Il nome della nazione in cui si trova
     * @return L'ID della città (nuova o già esistente)
     * @throws SQLException errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static int inserisciCitta(Connection conn, String nome, String nazione) throws SQLException {
        String check = "SELECT id_citta FROM CITTA WHERE nome = ? AND nome_nazione = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        }

        String sql = "INSERT INTO CITTA (nome, nome_nazione) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Citta: " + nome);
    }

    /**
     * Inserisce una nuova coppia di coordinate (latitudine e longitudine) nel database.
     * @param conn La connessione al database
     * @param lat La latitudine geografica
     * @param lon La longitudine geografica
     * @return L'ID generato per le coordinate
     * @throws SQLException errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static int inserisciCoordinate(Connection conn, double lat, double lon) throws SQLException {
        String sql = "INSERT INTO COORDINATE (latitudine, longitudine) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Coordinate");
    }

    /**
     * Registra un nuovo Luogo: via, città e coordinate.
     * @param conn La connessione al database
     * @param via L'indirizzo del luogo
     * @param idCitta L'ID della città associata
     * @param idCoord L'ID delle coordinate associate
     * @return L'ID generato per il luogo
     * @throws SQLException errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static int inserisciLuogo(Connection conn, String via, int idCitta, int idCoord) throws SQLException {
        String sql = "INSERT INTO LUOGO (via, id_citta, id_coordinate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (via.length() > 255) via = via.substring(0, 255);
            ps.setString(1, via);
            ps.setInt(2, idCitta);
            ps.setInt(3, idCoord);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Luogo");
    }

    /**
     *  inesrisce il ristorante nel db
     * @param conn connessione al db
     * @param nome nome del ristorante
     * @param telefono telefono del ristorante
     * @param sitoWeb sito_web del ristorante
     * @param prezzo prezzo medio
     * @param idUtente id del risoratore (proprietario)
     * @param idLuogo id del luogo
     * @param stelle numero di stelle michelin
     * @param delivery boolean se abilitato il delivery
     * @param prenotazione_online boolean se abilitata la prenotazione online
     * @return id del ristoratore
     * @throws SQLException
     * @author Elia Toschi
     */
    private static int inserisciRistorante(Connection conn, String nome, String telefono, String sitoWeb, String prezzo, int idUtente, int idLuogo, int stelle, boolean delivery, boolean prenotazione_online) throws SQLException {

        String checkSql = "SELECT id_ristorante FROM RISTORANTE WHERE nome = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, nome);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                return rsCheck.getInt(1);
            }
        }
        String sql = "INSERT INTO RISTORANTE (nome, telefono, sito_web, delivery, prenotazione_online, fascia_prezzo, id_utente, id_luogo, stelle_michelin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, telefono);
            ps.setString(3, sitoWeb);
            ps.setBoolean(4, delivery);
            ps.setBoolean(5, prenotazione_online);
            ps.setString(6, prezzo);
            ps.setInt(7, idUtente);
            ps.setInt(8, idLuogo);
            ps.setInt(9, stelle);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        throw new SQLException("Errore creazione Ristorante");
    }
    /**
     * Inserisce una nuova tipologia di cucina (es. "Italiana", "Pizza") nel database.
     * Ignora l'operazione se la tipologia è già registrata.
     * @param conn La connessione al database
     * @param nome Il nome della tipologia di cucina
     * @throws SQLException In caso di errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static void inserisciTipoCucina(Connection conn, String nome) throws SQLException {
        String sql = "INSERT INTO TIPO_CUCINA (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
        }
    }

    /**
     * Collega il ristorante e una tipologia di cucina.
     * @param conn La connessione al database
     * @param idRistorante L'ID del ristorante
     * @param nomeCucina Il nome della tipologia di cucina
     * @throws SQLException errore durante l'esecuzione della query
     * @author Elia Toschi
     */
    private static void collegaRistoranteCucina(Connection conn, int idRistorante, String nomeCucina) throws SQLException {
        String sql = "INSERT INTO RISTORANTE_TIPO_CUCINA (id_ristorante, nome_tipo_cucina) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRistorante);
            ps.setString(2, nomeCucina);
            ps.executeUpdate();
        }
    }

    /**
     * Prende gli utenti dal file users.csv e li importa nel db
     * @param conn
     * @author Elia Toschi
     */
    private static void importaUtenti(Connection conn) {
        System.out.println("Inizio importazione Utenti...");
        try (InputStream is = ImportaDati.class.getClassLoader().getResourceAsStream("db/migration/users.csv")) {
            if (is == null) {
                System.err.println("File users.csv non trovato!");
                return;
            }

            try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                reader.readNext();
                String[] parts;

                while ((parts = reader.readNext()) != null) {
                    if (parts.length < 6) continue;
                    String email = parts[0].trim();
                    String password = parts[1].trim();
                    String nome = parts[2].trim();
                    String cognome = parts[3].trim();
                    String citta = parts[4].trim();
                    boolean isRisto = parts[5].trim().equalsIgnoreCase("true");
                    java.sql.Date dataNascita = null;
                    if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                        String rawData = parts[8].replace("\"", "").trim();
                        try {
                            java.time.LocalDate ld = java.time.LocalDate.parse(rawData, java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"));
                            dataNascita = java.sql.Date.valueOf(ld);
                        } catch (Exception e) {
                            System.err.println("Errore formato data per: " + email);
                        }
                    }
                    inserisciNazione(conn, "Italia");
                    int idCitta = inserisciCitta(conn, citta, "Italia");
                    int idCoord = inserisciCoordinate(conn, 0.0, 0.0);
                    int idLuogo = inserisciLuogo(conn, "Via Roma 1", idCitta, idCoord);
                    String sqlUser = "INSERT INTO UTENTE (email, password, nome, cognome, is_ristoratore, id_luogo_vive, data_nascita) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (email) DO NOTHING RETURNING id_utente";
                    int idUtenteCreato = -1;
                    //TODO psw da generare l'hash
                    try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                        ps.setString(1, email);
                        ps.setString(2, password);
                        ps.setString(3, nome);
                        ps.setString(4, cognome);
                        ps.setBoolean(5, isRisto);
                        ps.setInt(6, idLuogo);
                        ps.setDate(7, dataNascita);

                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) idUtenteCreato = rs.getInt(1);
                    }

                    if (idUtenteCreato != -1) {
                        if (parts.length > 6 && !parts[6].trim().isEmpty() && !parts[6].trim().equalsIgnoreCase("NULL")) {
                            String[] preferiti = parts[6].split("-");
                            String sqlPref = "INSERT INTO PREFERITO (id_utente, id_ristorante) VALUES (?, ?) ON CONFLICT DO NOTHING";
                            try (PreparedStatement psPref = conn.prepareStatement(sqlPref)) {
                                for (String p : preferiti) {
                                    if (!p.trim().isEmpty()) {
                                        psPref.setInt(1, idUtenteCreato);
                                        psPref.setInt(2, Integer.parseInt(p.trim()));
                                        psPref.executeUpdate();
                                    }
                                }
                            }
                        }

                        if (parts.length > 7 && !parts[7].trim().isEmpty() && !parts[7].trim().equalsIgnoreCase("NULL")) {
                            String[] miei = parts[7].split("-");
                            String sqlMiei = "UPDATE RISTORANTE SET id_utente = ? WHERE id_ristorante = ?";
                            try (PreparedStatement psMiei = conn.prepareStatement(sqlMiei)) {
                                for (String m : miei) {
                                    if (!m.trim().isEmpty()) {
                                        psMiei.setInt(1, idUtenteCreato);
                                        psMiei.setInt(2, Integer.parseInt(m.trim()));
                                        psMiei.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Importazione Utenti completata!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prende le recensioni e le recensioni nel file recensioni.csv
     * @param conn
     * @author Elia Toschi
     */
    private static void importaRecensioni(Connection conn) {
        System.out.println("Inizio importazione Recensioni...");
        try (InputStream is = ImportaDati.class.getClassLoader().getResourceAsStream("db/migration/recensioni.csv")) {

            if (is == null) {
                System.err.println("File recensioni.csv non trovato!");
                return;
            }

            try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                reader.readNext();
                String[] parts;

                while ((parts = reader.readNext()) != null) {
                    if (parts.length < 5) continue;

                    int stelle = Integer.parseInt(parts[0].trim());
                    String testo = parts[1].trim();
                    // Converte in Timestamp SQL
                    Timestamp dataOra =Timestamp.valueOf(parts[2].trim().replace("T", " "));
                    int idUtente = Integer.parseInt(parts[3].trim());
                    int idRistorante = Integer.parseInt(parts[4].trim());

                    String sqlRec = "INSERT INTO RECENSIONE (data_ora, testo, numero_stelle, id_utente, id_ristorante) VALUES (?, ?, ?, ?, ?) RETURNING id";
                    int idRecensioneCreata = -1;

                    try (PreparedStatement ps = conn.prepareStatement(sqlRec)) {
                        ps.setTimestamp(1, dataOra);
                        ps.setString(2, testo);
                        ps.setInt(3, stelle);
                        ps.setInt(4, idUtente);
                        ps.setInt(5, idRistorante);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) idRecensioneCreata = rs.getInt(1);
                    }

                    if (idRecensioneCreata != -1 && parts.length > 5 && !parts[5].trim().isEmpty() && !parts[5].trim().equalsIgnoreCase("NULL")) {
                        String testoRisposta = parts[5].trim();


                        int idProprietario = 1;
                        try (PreparedStatement ps = conn.prepareStatement("SELECT id_utente FROM RISTORANTE WHERE id_ristorante = ?")) {
                            ps.setInt(1, idRistorante);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) idProprietario = rs.getInt(1);
                        }

                        String sqlRisp = "INSERT INTO RISPOSTA (id_recensione, id_utente, testo_risposta) VALUES (?, ?, ?)";
                        try (PreparedStatement psRisp = conn.prepareStatement(sqlRisp)) {
                            psRisp.setInt(1, idRecensioneCreata);
                            psRisp.setInt(2, idProprietario);
                            psRisp.setString(3, testoRisposta);
                            psRisp.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Importazione Recensioni completata!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


