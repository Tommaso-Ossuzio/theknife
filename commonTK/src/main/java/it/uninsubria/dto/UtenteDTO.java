package it.uninsubria.dto;

import java.io.Serializable;
import java.util.Date;

public class UtenteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public Date getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(Date dataNascita) {
        this.dataNascita = dataNascita;
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
