/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Utente proprietario di uno o più ristoranti.
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class RistoratoreDTO extends UtenteDTO implements Serializable
{
    private static final long serialVersionUID = 1L;


    public RistoratoreDTO(int idUtente, String nome, String cognome, String email, Date dataNascita, LuogoDTO luogo) {
        super(idUtente, nome, cognome, email, dataNascita, luogo);
    }

    /**
     * Ristoratore di cui serve soltanto l'identificativo, per indicare chi
     * possiede un ristorante o chi ha scritto una risposta.
     * @author Celestino Resteghini
     */
    public RistoratoreDTO(int idUtente) {
        super(idUtente);
    }

    @Override
    public String toString() {
        return "RistoratoreDTO{} " + super.toString();
    }
}
