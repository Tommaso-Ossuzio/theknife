package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Rappresenta il modello delle città
 * @author Elia Toschi
 */
public class CittaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id_citta;
    private String nome;
    private String nazione;

    public CittaDTO(int id, String nome, String nazione) {
        this.nazione = nazione;
        this.nome = nome;
        this.id_citta = id;
    }

    public CittaDTO(String nome) {
        this.nome = nome;
    }


    public int getId_citta() {
        return id_citta;
    }

    public void setId_citta(int id_citta) {
        this.id_citta = id_citta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    @Override
    public String toString() {
        return "CittaDTO{" +
                "id=" + id_citta +
                ", nome='" + nome + '\'' +
                ", nazione='" + nazione + '\'' +
                '}';
    }
}
