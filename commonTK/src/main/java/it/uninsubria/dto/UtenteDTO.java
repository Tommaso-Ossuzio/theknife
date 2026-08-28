package it.uninsubria.dto;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Rappresenta il modello degli utenti. Contiene il luogoDTO della sua residenza
 * @author Elia Toschi
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
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.dataNascita = dataNascita;
        this.luogo = luogo;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Controlla che l'email abbia la forma qualcosa@qualcosa.qualcosa.
     * @param email indirizzo da controllare
     * @return true se il formato e' valido
     * @author Matteo Franguelli
     */
    public static boolean emailValida(String email) {
        return email != null && FORMATO_EMAIL.matcher(email.trim()).matches();
    }

    public Date getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(Date dataNascita) {
        this.dataNascita = dataNascita;
    }

    /**
     * Controlla che la data di nascita sia plausibile: non nel futuro e con
     * un'eta' compresa fra 14 e 120 anni. La data e' facoltativa, quindi null
     * e' accettato.
     * @param dataNascita data da controllare
     * @return true se la data e' accettabile
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

    public void setLuogo(LuogoDTO luogo) {
        this.luogo = luogo;
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
