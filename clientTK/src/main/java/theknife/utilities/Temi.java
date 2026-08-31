/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.utilities;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Gestisce il tema chiaro/scuro dell'applicazione.
 * @author Matteo Franguelli
 */
public final class Temi {

    /** I due temi disponibili. */
    public enum Tema {
        CHIARO,
        SCURO
    }

    /** Percorso del foglio di stile che contiene la palette scura. */
    private static final String PERCORSO_TEMA_SCURO =
            "/it/unininsubria/theknifeui/ui/javafx/view/theme-dark.css";

    /** Tema attivo. L'applicazione parte sempre in chiaro. */
    private static Tema temaCorrente = Tema.CHIARO;

    /** Scene attualmente aperte, da riverniciare a ogni cambio di tema. */
    private static final List<WeakReference<Scene>> sceneAperte = new ArrayList<>();

    /** Classe di sole utilità: non va istanziata. */
    private Temi() {
    }

    /**
     * Registra una scena appena creata e le applica subito il tema in uso.
     * Va chiamato ogni volta che si apre una finestra, altrimenti quella
     * finestra resterebbe chiara anche col tema scuro attivo.
     *
     * @param scena scena da registrare
     * @author Matteo Franguelli
     */
    public static void registra(Scene scena) {
        if (scena == null) return;
        sceneAperte.add(new WeakReference<>(scena));
        applicaA(scena);
    }

    /**
     * Passa dal tema chiaro a quello scuro e viceversa, aggiornando
     * tutte le finestre aperte, non solo quella da cui è partito il comando.
     *
     * @author Matteo Franguelli
     */
    public static void alterna() {
        temaCorrente = (temaCorrente == Tema.CHIARO) ? Tema.SCURO : Tema.CHIARO;

        Iterator<WeakReference<Scene>> iteratore = sceneAperte.iterator();
        while (iteratore.hasNext()) {
            Scene scena = iteratore.next().get();
            if (scena == null) {
                iteratore.remove();
            } else {
                applicaA(scena);
            }
        }
    }

    /**
     * Applica il tema in uso a una scena.
     * Serve anche quando si sostituisce la radice di una scena già registrata,
     * per esempio passando dalla schermata di benvenuto a quella principale.
     *
     * @param scena scena da aggiornare
     * @author Matteo Franguelli
     */
    public static void applicaA(Scene scena) {
        if (scena == null) return;

        Parent radice = scena.getRoot();
        if (radice == null) return;

        URL url = Temi.class.getResource(PERCORSO_TEMA_SCURO);
        if (url == null) return;
        String foglio = url.toExternalForm();

        radice.getStylesheets().remove(foglio);
        if (temaCorrente == Tema.SCURO) {
            radice.getStylesheets().add(foglio);
        }
    }

    /**
     * Indica se è attivo il tema scuro.
     * @author Matteo Franguelli
     */
    public static boolean isScuro() {
        return temaCorrente == Tema.SCURO;
    }

    /**
     * Restituisce il simbolo da mostrare sul pulsante di cambio tema:
     * il sole quando si è al buio, la luna quando si è in chiaro, cioè
     * sempre il tema verso cui si andrebbe premendolo.
     *
     * @author Matteo Franguelli
     */
    public static String simboloPulsante() {
        return isScuro() ? "☀" : "☾";
    }

    /**
     * Restituisce la descrizione dell'azione del pulsante, usata come
     * suggerimento e come nome accessibile per i lettori di schermo.
     *
     * @author Matteo Franguelli
     */
    public static String descrizionePulsante() {
        return isScuro() ? "Passa al tema chiaro" : "Passa al tema scuro";
    }
}
