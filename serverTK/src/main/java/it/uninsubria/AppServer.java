package it.uninsubria;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

public class AppServer {
    public static void main(String[] args) {
        System.out.println("Avvio del Server TheKnife...");

        DatabaseConfig.createDatabaseIfMissing();

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(DatabaseConfig.getTargetUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())
                    .load();
            //legge il file
            flyway.migrate();

            System.out.println("Tabelle pronte nel database theknife_db!");

        } catch (FlywayException e) {
            System.err.println("Errore durante la configurazione del Database:");
            e.printStackTrace();
        }
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                DatabaseConfig.getTargetUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword())) {

            ImportatoreDati.importa(conn);

        } catch (Exception e) {
            System.err.println("Errore durante l'importazione:");
            e.printStackTrace();
        }
    }
}