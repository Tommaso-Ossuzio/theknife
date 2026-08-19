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
    private static String nomeFileUtenti = "users.csv";
    private static String nomeFileRecensioni = "recensioni.csv";

    private static final String NOME_CARTELLA_DOC = "data";

    private static final String percorsoBase = System.getProperty("user.dir") + File.separator + nomeCartella + File.separator;
    private static final String percorsoFileRecensioni = percorsoBase + nomeFileRecensioni;

    /**
     * Cerca nel file users.csv la città associata allo username.
     * @author Matteo Franguelli
     */
    public static String recuperaCittaUtente(String usernameTarget) {
        File file = new File("data", "users.csv");
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea = br.readLine(); // Salta header
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] parti = linea.split(";");
                if (parti.length > 0 && pulisci(parti[0]).equalsIgnoreCase(usernameTarget)) {
                    if (parti.length > 4) {
                        return pulisci(parti[4]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Restituisce gli id dei ristoranti di cui l'utente indicato è proprietario.
     * <p>
     * Nel file degli utenti i ristoranti posseduti stanno tutti nell'ultima
     * colonna, separati da un trattino; qui vengono riportati a numeri.
     *
     * @param usernameTarget username del ristoratore
     * @return la lista degli id, vuota se l'utente non possiede ristoranti
     * @author Matteo Franguelli
     */
    public static LinkedList<Integer> recuperaIdRistorantiUtente(String usernameTarget) {
        LinkedList<Integer> identificativi = new LinkedList<>();

        File file = new File("data", "users.csv");
        if (!file.exists() || usernameTarget == null) return identificativi;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea = br.readLine(); // Salta header
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] parti = linea.contains(";") ? linea.split(";") : linea.split(",");
                if (parti.length == 0 || !pulisci(parti[0]).equalsIgnoreCase(usernameTarget)) continue;

                if (parti.length > 9 && !parti[9].isBlank()) {
                    for (String pezzo : parti[9].split("-")) {
                        try {
                            identificativi.add(Integer.valueOf(pulisci(pezzo)));
                        } catch (NumberFormatException ignorato) {
                            // Voce non numerica: viene saltata
                        }
                    }
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return identificativi;
    }

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
     * Metodo di ricerca dell'ID di un utente a partire da suo username
     * @param username
     * @author Elia Toschi
     */
    public static int recuperaId(String username)
    {
        File cartellaDoc = new File(NOME_CARTELLA_DOC);
        File fileUtenti = new File(cartellaDoc, nomeFileUtenti);
        int id=0;
        try(BufferedReader br = new BufferedReader(new FileReader(fileUtenti, StandardCharsets.UTF_8))) {
            String linea;
            br.readLine();
            while ((linea = br.readLine()) != null ) {

                String[] parti = linea.split(";");

                if(parti[0].equals(username))
                {
                    id= Integer.valueOf(parti[7].trim());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Legge le recensioni dal file.
     * @author Matteo Franguelli
     */
    public static LinkedList<Recensione> leggiRecensioni() {
        LinkedList<Recensione> lista = new LinkedList<>();
        File file = new File(percorsoFileRecensioni);

        if (!file.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank() || linea.toLowerCase().startsWith("n_stelle")) continue;

                String[] parti = linea.contains(";") ? linea.split(";") : linea.split(",");

                if (parti.length >= 5) {
                    try {
                        int stelle = Integer.parseInt(pulisci(parti[0]));
                        String testo = pulisci(parti[1]);
                        // parti[2] era data
                        int idUtente = Integer.parseInt(pulisci(parti[3]));
                        int idRistorante = Integer.parseInt(pulisci(parti[4]));

                        Recensione r = new Recensione(stelle, testo, idUtente, idRistorante);
                        lista.add(r);

                    } catch (Exception e) {
                        System.err.println("Errore lettura riga: " + linea);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
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

            // Il file è cambiato: media e conteggi mostrati nelle card vanno ricalcolati
            GestioneRecensioni.getInstance().ricaricaIndice();
        }
    }

}