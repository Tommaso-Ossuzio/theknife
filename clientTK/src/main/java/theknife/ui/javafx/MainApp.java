package theknife.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import theknife.model.GestioneRichieste;
import theknife.utilities.Finestre;
import theknife.utilities.Temi;

import java.io.IOException;
import java.net.URL;
import java.awt.Taskbar;
import javax.imageio.ImageIO;

//TODO da rivedere

/**
 * Applicazione JavaFX del client: apre la finestra sulla schermata di benvenuto.
 * @author Matteo Franguelli
 * @version 2
 */
public class MainApp extends Application {

    /**
     * Prepara la sessione come ospite e mostra la schermata di benvenuto.
     * @param finestra finestra principale dell'applicazione
     * @throws Exception se il caricamento della scena fallisce
     * @author Matteo Franguelli
     */
    @Override
    public void start(Stage finestra) throws Exception {
        // Utente impostato come "ospite" di default
        Session.getInstance().login(null, Session.Role.GUEST);

        URL urlFxml = MainApp.class.getResource(
                "/it/unininsubria/theknifeui/ui/javafx/view/welcome.fxml");
        if (urlFxml == null) {
            throw new IllegalStateException("welcome.fxml non trovato nel classpath!");
        }

        FXMLLoader caricatore = new FXMLLoader(urlFxml);
        Parent radice = caricatore.load();

        Rectangle2D schermo = Screen.getPrimary().getVisualBounds();
        double larghezzaDinamica= schermo.getWidth()*0.94;
        double altezzaDinamica= schermo.getHeight()*0.91;


        Scene scena = new Scene(radice, larghezzaDinamica, altezzaDinamica);

        Temi.registra(scena);

        finestra.setTitle("TheKnife");

        finestra.centerOnScreen();

        // Carichiamo l'icona dell'applicazione
        URL urlIcona = MainApp.class.getResource(
                "/it/unininsubria/theknifeui/ui/javafx/img/logo_theknife-light.png");
        if (urlIcona != null) {
            // Icona per la finestra JavaFX
            Image iconaFx = new Image(urlIcona.toExternalForm());
            finestra.getIcons().add(iconaFx);
            // Icona per la taskbar/dock (se supportata dal sistema)
            try {
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar barraAttivita = Taskbar.getTaskbar();
                    if (barraAttivita.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        java.awt.Image iconaAwt = ImageIO.read(urlIcona);
                        barraAttivita.setIconImage(iconaAwt);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        finestra.setScene(scena);
        finestra.show();

        finestra.setOnCloseRequest(event -> {
            try{
                //TODO va messa solo qua?
                GestioneRichieste.getInstance().chiudiConnessione();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            javafx.application.Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Avvia l'applicazione.
     * @author Matteo Franguelli
     */
    public static void main(String[] args) {
        launch(args);
    }
}
