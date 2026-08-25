package it.uninsubria;


import java.io.*;
import java.net.*;

public class AppServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Avvio del Server TheKnife...");

        DatabaseConfig.inizializzaDatabaseCompleto();
        exec();

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
                new SlaveThread(socket).start();
            }
        } finally {
            s.close();
        }
    }
}