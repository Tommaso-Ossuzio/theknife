package it.uninsubria;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * La classe serve per inizializzare tutti i dati nel database
 * @author Elia Toschi
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();
    public static final String DB_NAME = "theknife_db";


    static {
        try {
            // Prima cerca il file "db.properties" ESTERNO
            // todo bisogna avere il file properties all'esterno del .jar
            File externalFile = new File("db.properties");

            if (externalFile.exists()) {
                System.out.println("Caricamento configurazione DB da file esterno...");
                try (InputStream input = new FileInputStream(externalFile)) {
                    props.load(input);
                }
            } else {
                //  Se non c'è, cerca quello INTERNO
                System.out.println("File esterno non trovato. Caricamento configurazione DB da resources interne...");
                try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (input != null) {
                        props.load(input);
                    } else {
                        System.err.println("ATTENZIONE: Nessun db.properties trovato! Verranno usati i valori di default.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della configurazione del database:");
            e.printStackTrace();
        }
    }

    /**
     * Imposta le credenziali indicate all'avvio del server.
     * @author Elia Toschi
     */
    public static void configura(String host, String porta, String utente, String password) {
        props.setProperty("db.url.default", "jdbc:postgresql://" + host + ":" + porta + "/postgres");
        props.setProperty("db.user", utente);
        props.setProperty("db.password", password);
    }

    // --- GETTERS PER LE CREDENZIALI ---

    public static String getDefaultUrl() {
        // restituisce l'URL del database di sistema
        return props.getProperty("db.url.default", "jdbc:postgresql://localhost:5432/postgres");
    }

    public static String getTargetUrl() {
        // Deriva l'URL target sostituendo il db predefinito con thekinfe_db
        String defaultUrl = getDefaultUrl();
        int lastSlashIndex = defaultUrl.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return defaultUrl.substring(0, lastSlashIndex + 1) + DB_NAME;
        }

        return "jdbc:postgresql://localhost:5432/" + DB_NAME;
    }

    public static String getUser() {
        return props.getProperty("db.user", "postgres");

    }

    public static String getPassword() {
        return props.getProperty("db.password", "la_tua_password_qui");
    }

    // --- METODI PER GESTIONE DATABASE ---

    /**
     * Controlla se il database theknife_db esiste già.
     * @author Elia Toschi
     */
    public static boolean databaseExists() {
        boolean exists = false;
        try (Connection conn = DriverManager.getConnection(getDefaultUrl(), getUser(), getPassword())) {
            String queryControllo = "SELECT 1 FROM pg_database WHERE datname = ?";
            try (PreparedStatement ps = conn.prepareStatement(queryControllo)) {
                ps.setString(1, DB_NAME);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next(); // Se rs.next() è true, il database c'è.
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il controllo dell'esistenza del database.");
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * Esegue fisicamente la query CREATE DATABASE theknife_db.
     * @author Elia Toschi
     */
    public static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(getDefaultUrl(), getUser(), getPassword())) {
            System.out.println("Creazione del database '" + DB_NAME + "' in corso...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE " + DB_NAME);
                System.out.println("Database creato con successo!");
            }
        } catch (SQLException e) {
            System.err.println("Impossibile creare il database.");
            e.printStackTrace();
        }
    }

    /**
     * Metodo di utilità che combina i due controlli precedenti (perfetto da chiamare all'avvio del server).
     * @author Elia Toschi
     */
    public static void createDatabaseIfMissing() {
        if (!databaseExists()) {
            System.out.println("Il database '" + DB_NAME + "' non esiste.");
            createDatabase();
        } else {
            System.out.println("Il database '" + DB_NAME + "' esiste già.");
        }
    }

    /**
     * Il metodo che crea il db se non esiste e importa i dati
     * @author Elia Toschi
     */
    public static void inizializzaDatabaseCompleto() {
        java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(java.util.logging.Level.SEVERE);
        for (java.util.logging.Handler h : rootLogger.getHandlers()) {
            h.setLevel(java.util.logging.Level.SEVERE);
        }        createDatabaseIfMissing();

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(getTargetUrl(), getUser(), getPassword())
                    .load();
            flyway.migrate();
            System.out.println("Tabelle pronte nel database theknife_db!");
        } catch (FlywayException e) {
            System.err.println("Errore durante la migrazione delle tabelle:");
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(getTargetUrl(), getUser(), getPassword())) {
            ImportaDati.importa(conn);
        } catch (Exception e) {
            System.err.println("Errore durante l'importazione dei dati:");
            e.printStackTrace();
        }
    }}