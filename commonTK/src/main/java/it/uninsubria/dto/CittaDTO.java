package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Città in cui si trova un ristorante o un utente.
 * @author Elia Toschi
 * @author Celestino Resteghini
 */
public class CittaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id_citta;
    private String nome;
    private String nazione;

    /**
     * Città completa, come arriva dal database.
     * @author Elia Toschi
     */
    public CittaDTO(int id, String nome, String nazione) {
        this.nazione = nazione;
        this.nome = nome;
        this.id_citta = id;
    }

    /**
     * Città di cui si conosce solo il nome, come quella digitata dall'utente.
     * @author Elia Toschi
     */
    public CittaDTO(String nome) {
        this.nome = nome;
    }

    /**
     * Città non ancora salvata, quindi senza identificativo.
     * @author Celestino Resteghini
     */
    public CittaDTO(String nome, String nazione) {
        this.nome = nome;
        this.nazione = nazione;
    }

    public String getNome() {
        return nome;
    }

    public String getNazione() {
        return nazione;
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
