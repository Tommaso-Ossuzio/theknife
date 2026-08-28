package it.uninsubria.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Indica il modello del ristoratore e estende la classe UtenteDTO
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class RistoratoreDTO extends UtenteDTO implements Serializable
{
    private static final long serialVersionUID = 1L;


    public RistoratoreDTO(int idUtente, String nome, String cognome, String email, Date dataNascita, LuogoDTO luogo) {
        super(idUtente, nome, cognome, email, dataNascita, luogo);
    }

    public RistoratoreDTO(int idUtente) {
        super(idUtente);
    }

    @Override
    public String toString() {
        return "RistoratoreDTO{} " + super.toString();
    }
}
