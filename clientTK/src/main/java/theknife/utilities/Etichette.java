package theknife.utilities;

import it.uninsubria.dto.RistoranteDTO;
import javafx.scene.control.Label;
import theknife.model.Ristorante;

import java.util.Locale;

/**
 * Badge informativi usati nelle schermate che elencano ristoranti.
 * @author Matteo Franguelli
 */
public final class Etichette {

    /** Classe di sole utilità: non va istanziata. */
    private Etichette() {
    }

    /**
     * Crea un badge colorato (tipo di cucina, stelle Michelin, servizi).
     *
     * @param testo             testo mostrato dentro il badge
     * @param classeAggiuntiva  styleClass che ne determina il colore
     * @author Matteo Franguelli
     */
    public static Label creaBadge(String testo, String classeAggiuntiva) {
        Label etichetta = new Label(testo);
        etichetta.getStyleClass().add("tag");
        if (classeAggiuntiva != null && !"tag".equals(classeAggiuntiva)) {
            etichetta.getStyleClass().add(classeAggiuntiva);
        }
        return etichetta;
    }

    /**
     * Crea il badge con la media delle recensioni di un ristorante.
     * @param r ristorante di cui mostrare la media
     * @author Matteo Franguelli
     */
    public static Label creaBadgeMedia(Ristorante r) {
        double media = r.getMediaStelle();
        int recensioni = r.getNumRecensioni();

        return creaBadgeMedia(media, recensioni);
    }

    /**
     * Crea il badge della media per i ristoranti ricevuti dal nuovo flusso
     * server, rappresentati da {@link RistoranteDTO}.
     *
     * @param r ristorante di cui mostrare la media
     * @return badge della media o dell'assenza di recensioni
     *
     * @author Michele Viselli
     */
    public static Label creaBadgeMedia(RistoranteDTO r) {
        if (r == null) return creaBadge("Nessuna recensione", "tag-rating-none");

        double media = r.getMediaStelle() == null ? -1.0 : r.getMediaStelle();
        int recensioni = r.getNumeroRecensioni() == null ? 0 : r.getNumeroRecensioni();

        return creaBadgeMedia(media, recensioni);
    }

    private static Label creaBadgeMedia(double media, int recensioni) {

        if (recensioni <= 0 || media < 0) {
            return creaBadge("Nessuna recensione", "tag-rating-none");
        }

        String testo = String.format(Locale.ITALY, "★ %.1f  ·  %d %s",
                media, recensioni, recensioni == 1 ? "recensione" : "recensioni");

        String classe;
        if (media >= 4)      classe = "tag-rating-high";
        else if (media >= 2) classe = "tag-rating-mid";
        else                 classe = "tag-rating-low";

        return creaBadge(testo, classe);
    }

    /**
     * Crea il badge delle stelle Michelin, oppure null se il ristorante
     * non ne ha: i ristoranti senza stelle non mostrano alcun badge.
     *
     * @param r ristorante di cui mostrare il riconoscimento
     * @author Matteo Franguelli
     */
    public static Label creaBadgeMichelin(Ristorante r) {
        if (r.getAward() <= 0) return null;
        return creaBadge("★ " + (int) r.getAward() + " Michelin", "tag-michelin");
    }

    /**
     * Crea il badge delle stelle Michelin per un ristorante ricevuto dal
     * server tramite il nuovo flusso basato su {@link RistoranteDTO}.
     *
     * @param r ristorante di cui mostrare il riconoscimento
     * @return badge Michelin oppure {@code null} se il ristorante non ha stelle
     */
    public static Label creaBadgeMichelin(RistoranteDTO r) {
        if (r == null || r.getStelleMichelin() <= 0) return null;
        return creaBadge("★ " + r.getStelleMichelin() + " Michelin", "tag-michelin");
    }
}
