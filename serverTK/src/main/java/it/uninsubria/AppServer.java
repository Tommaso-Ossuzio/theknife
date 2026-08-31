/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria;


import java.io.*;
import java.net.*;

import javafx.application.Application;

/**
 * Classe padre che esegue gli slave thread
 * @author Celestino Resteghini
 */
public class AppServer {
    public static void main(String[] args) throws IOException {
        Application.launch(ServerApp.class, args);

    }

    /**
     * Metodo per la gestione di richieste al server
     * @author Celestino Resteghini
     * @throws IOException
     */
    public static void exec() throws IOException {
        ServerSocket s = new ServerSocket(8999);
        try {
            while (true) {
                Socket socket = s.accept();
                try {
                    new SlaveThread(socket).start();
                } catch (IOException | ClassNotFoundException e) {
                    // Una connessione chiusa prima dell'handshake non deve
                    // terminare il server (può capitare con probe o client
                    // interrotti durante l'avvio).
                    try { socket.close(); } catch (IOException ignored) { }
                    System.err.println("Connessione client non valida: " + e.getMessage());
                }
            }
        } finally {
            s.close();
        }
    }
}
