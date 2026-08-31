/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Credenziali che il client invia al server per l'accesso.
 * @author Michele Viselli
 */
public class AuthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    /**
     * @param password già cifrata con SHA-256
     * @author Michele Viselli
     */
    public AuthDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "AuthDTO{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
