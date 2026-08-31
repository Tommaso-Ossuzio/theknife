/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package theknife.ui.javafx;

import java.io.IOException;

/**
 * Schermata capace di reagire a un'autenticazione andata a buon fine.
 * @author Matteo Franguelli
 */
public interface ControllerAutenticazione {

    /**
     * Chiamato dopo un accesso o una registrazione riusciti.
     * @author Matteo Franguelli
     */
    void onLoginSuccess() throws IOException;
}
