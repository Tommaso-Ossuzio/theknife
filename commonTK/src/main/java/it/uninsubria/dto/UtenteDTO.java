/*
 Cognome     Nome       Matricola  Sede
 Franguelli  Matteo     761133     VA
 Toschi      Elia       760873     VA
 Resteghini  Celestino  760865     VA
 Viselli     Michele    763016     VA
*/
package it.uninsubria.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Utente registrato, con il luogo in cui vive.
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class UtenteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int ETA_MINIMA = 14;
    private static final int ETA_MASSIMA = 120;

    private int idUtente;
    private String nome;
    private String cognome;
    private String email;
    private Date dataNascita;
    private LuogoDTO luogo;
    private String password;

    //senza psw
    public UtenteDTO(int idUtente, String nome, String cognome, String email, Date dataNascita, LuogoDTO luogo) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.dataNascita = dataNascita;
        this.luogo = luogo;
    }

    //usato dal client
    public UtenteDTO(String nome, String cognome, String email, Date dataNascita, LuogoDTO luogo, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.dataNascita = dataNascita;
        this.luogo = luogo;
        this.password = password;
    }

    public UtenteDTO(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getPassword() {
        return password;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Controlla che l'email abbia la forma qualcosa@qualcosa.qualcosa.
     * @param email indirizzo da controllare
     * @return true se il formato è valido
     * @author Matteo Franguelli
     */
    public static boolean emailValida(String email) {
        return email != null && FORMATO_EMAIL.matcher(email.trim()).matches();
    }

    public Date getDataNascita() {
        return dataNascita;
    }

    /**
     * Controlla che la data di nascita non sia nel futuro e che l'età sia fra 14 e 120 anni.
     * @param dataNascita data da controllare, può essere null perché il campo è facoltativo
     * @return true se la data è accettabile
     * @author Matteo Franguelli
     */
    public static boolean dataNascitaValida(Date dataNascita) {
        if (dataNascita == null) return true;

        LocalDate nascita = Instant.ofEpochMilli(dataNascita.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate oggi = LocalDate.now();

        if (nascita.isAfter(oggi)) return false;

        int anni = Period.between(nascita, oggi).getYears();
        return anni >= ETA_MINIMA && anni <= ETA_MASSIMA;
    }

    public LuogoDTO getLuogo() {
        return luogo;
    }

    @Override
    public String toString() {
        return "UtenteDTO{" +
                "idUtente=" + idUtente +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", dataNascita=" + dataNascita +
                ", luogo=" + luogo +
                '}';
    }
}
