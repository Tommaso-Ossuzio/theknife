package theknife.utilities;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Utilità condivise fra le schermate.
 * @author Matteo Franguelli
 */
public class Utility {

    /**
     * Permette di confermare con il tasto Invio, oltre che con il pulsante.
     * @param conferma pulsante di conferma della schermata
     * @param campi campi in cui Invio deve valere come conferma
     * @author Matteo Franguelli
     */
    public static void confermaConInvio(Button conferma, Node... campi) {
        if (conferma == null) return;

        conferma.setDefaultButton(true);

        for (Node campo : campi) {
            if (campo == null) continue;

            campo.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
                if (evento.getCode() != KeyCode.ENTER || conferma.isDisabled()) return;
                if (campo instanceof ComboBox<?> tendina && tendina.isShowing()) return;

                conferma.fire();
                evento.consume();
            });
        }
    }
}
