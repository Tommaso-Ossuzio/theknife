package theknife.ui.javafx;

/**
 * Schermata capace di reagire a un'autenticazione andata a buon fine.
 * @author Matteo Franguelli
 */
public interface ControllerAutenticazione {

    /**
     * Chiamato dopo un accesso o una registrazione riusciti.
     * @author Matteo Franguelli
     */
    void onLoginSuccess();
}
