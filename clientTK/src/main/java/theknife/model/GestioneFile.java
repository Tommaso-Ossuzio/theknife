package theknife.model;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

//TODO: da cancellare, creare gestioneDB fatta decentemente

/**
 * @author Elia Toschi
 * @author Celestino Resteghini
 * @author Matteo Franguelli
 * La classe si occupa della gestione dei File
 */
public class GestioneFile {

    private static String nomeCartella = "data";
    private static String nomeFileRecensioni = "recensioni.csv";

    private static final String percorsoBase = System.getProperty("user.dir") + File.separator + nomeCartella + File.separator;
    private static final String percorsoFileRecensioni = percorsoBase + nomeFileRecensioni;


    /**
     * Metodo di utilità per pulire le stringhe lette da CSV.
     * Rimuove spazi iniziali/finali, doppi apici e punto e virgola.
     * @author Matteo Franguelli
     */
    private static String pulisci(String s) {
        if (s == null) return "";
        return s.trim().replace("\"", "").replace(";", "");
    }

    /**
     * Rimuove la recensione dal file CSV confrontando idUtente, Ristorante, Voto e Testo.
     * La data viene ignorata nel confronto.
     * @param idRistorante
     * @param idUtente
     * @param testo
     * @param voto
     * @author Matteo Franguelli
     */
    public static void rimuoviRecensione(int idUtente, int idRistorante, int voto, String testo) {
        File file = new File(percorsoFileRecensioni);
        List<String> righeDaSalvare = new LinkedList<>();
        boolean trovata = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank() || linea.toLowerCase().startsWith("n_stelle")) {
                    righeDaSalvare.add(linea);
                    continue;
                }
                String[] parti = linea.contains(";") ? linea.split(";") : linea.split(",");
                boolean daEliminare = false;

                if (parti.length >= 5) {
                    try {
                        int rVoto = Integer.parseInt(pulisci(parti[0]));
                        String rTesto = pulisci(parti[1]);
                        int rIdUtente = Integer.parseInt(pulisci(parti[3]));
                        int rIdRistorante = Integer.parseInt(pulisci(parti[4]));

                        if (!trovata &&
                                rIdUtente == idUtente &&
                                rIdRistorante == idRistorante &&
                                rVoto == voto &&
                                rTesto.equals(testo)) {

                            daEliminare = true;
                            trovata = true;
                        }
                    } catch (Exception e) {
                    }
                }

                if (!daEliminare) {
                    righeDaSalvare.add(linea);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (trovata) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                for (String riga : righeDaSalvare) {
                    bw.write(riga);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            GestioneRecensioni.getInstance().ricaricaIndice();
        }
    }

}