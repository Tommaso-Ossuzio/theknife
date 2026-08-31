package it.uninsubria.dto;

import java.io.Serializable;

/**
 * Criteri della ricerca dei ristoranti: i campi lasciati a null non vengono applicati.
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

    /**
     * @param luogo città da cui cercare: la ricerca comprende anche i ristoranti entro 10 km
     * @author Michele Viselli
     */
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

    public String getCucina() {
        return cucina;
    }

    public String getFasciaPrezzo() {
        return fasciaPrezzo;
    }

    public Double getMediaStelleMinima() {
        return mediaStelleMinima;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public Boolean getPrenotazione() {
        return prenotazione;
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
