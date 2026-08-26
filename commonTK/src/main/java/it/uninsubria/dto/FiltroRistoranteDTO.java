package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Parametri della richiesta di ricerca dei ristoranti ({@code FILTRO}).
 *
 * <p>Quando {@code luogo} è valorizzato, il server usa un ristorante della
 * città indicata come riferimento e restituisce sia i ristoranti della città
 * sia quelli entro 10 km. Quando è {@code null}, non viene applicato alcun
 * criterio geografico e può essere restituito l'intero catalogo. Anche gli
 * altri campi possono essere {@code null}: in quel caso il server non applica
 * il relativo criterio di ricerca.</p>
 *
 * @author Michele Viselli
 */
public class FiltroRistoranteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String luogo;
    private String cucina;
    private String fasciaPrezzo;
    private Double mediaStelleMinima;
    private Boolean delivery;
    private Boolean prenotazione;

    public FiltroRistoranteDTO(
            String luogo,
            String cucina,
            String fasciaPrezzo,
            Double mediaStelleMinima,
            Boolean delivery,
            Boolean prenotazione) {

        this.luogo = luogo;
        this.cucina = cucina;
        this.fasciaPrezzo = fasciaPrezzo;
        this.mediaStelleMinima = mediaStelleMinima;
        this.delivery = delivery;
        this.prenotazione = prenotazione;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public String getCucina() {
        return cucina;
    }

    public void setCucina(String cucina) {
        this.cucina = cucina;
    }

    public String getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public void setFasciaPrezzo(String fasciaPrezzo) {
        this.fasciaPrezzo = fasciaPrezzo;
    }

    public Double getMediaStelleMinima() {
        return mediaStelleMinima;
    }

    public void setMediaStelleMinima(Double mediaStelleMinima) {
        this.mediaStelleMinima = mediaStelleMinima;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public Boolean getPrenotazione() {
        return prenotazione;
    }

    public void setPrenotazione(Boolean prenotazione) {
        this.prenotazione = prenotazione;
    }

    @Override
    public String toString() {
        return "FiltroRistoranteDTO{" +
                "luogo='" + luogo + '\'' +
                ", cucina='" + cucina + '\'' +
                ", fasciaPrezzo='" + fasciaPrezzo + '\'' +
                ", mediaStelleMinima=" + mediaStelleMinima +
                ", delivery=" + delivery +
                ", prenotazione=" + prenotazione +
                '}';
    }
}
