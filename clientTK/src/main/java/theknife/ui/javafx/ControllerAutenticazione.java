package theknife.ui.javafx;

/**
 * Schermata capace di reagire a un'autenticazione andata a buon fine.
 * <p>
 * Le finestre di accesso e di registrazione non sanno da dove sono state
 * aperte: al termine avvisano semplicemente chi le ha aperte, che rilegge la
 * {@link Session} e si riallinea. Prima esisteva solo la schermata principale
 * e il riferimento era di tipo {@code MainController}; da quando esiste anche
 * la schermata di benvenuto servono entrambe, e questa interfaccia è ciò che
 * le accomuna.
 *
 * @author Matteo Franguelli
 */
public interface ControllerAutenticazione {

    /**
     * Chiamato dopo un accesso o una registrazione riusciti.
     * @author Matteo Franguelli
     */
    void onLoginSuccess();
}
