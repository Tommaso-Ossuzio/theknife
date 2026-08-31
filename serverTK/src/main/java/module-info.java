/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
module serverTK {
    requires commonTK;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires flyway.core;
    requires com.opencsv;
    requires org.postgresql.jdbc;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;
    requires org.apache.commons.text;

    exports it.uninsubria;
    exports it.uninsubria.dao;

    opens it.uninsubria to javafx.fxml;
    opens db.migration;
}
