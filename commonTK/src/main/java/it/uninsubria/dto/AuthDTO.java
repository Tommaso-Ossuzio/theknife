package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Dati utilizzati dal client per la richiesta di autenticazione ({@code LOG}).
 *
 * @author Michele Viselli
 */
public class AuthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

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
