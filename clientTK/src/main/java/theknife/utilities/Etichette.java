package theknife.utilities;

import it.uninsubria.dto.RistoranteDTO;
import javafx.scene.control.Label;
import theknife.model.Ristorante;

import java.util.Locale;

/**
 * Badge informativi usati nelle schermate che elencano ristoranti.
 * <p>
 * Stanno qui, e non dentro un singolo controller, perché le stesse etichette
 * compaiono sia nelle card dell'elenco sia nella dashboard del ristoratore:
 * tenerne una sola versione evita che i due elenchi mostrino la stessa
 * informazione in due modi diversi.
 *
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
     * Converte la fascia testuale usata dal database nei simboli mostrati
     * dall'interfaccia. Il valore originale nel DTO non viene modificato.
     *
     * @param fasciaPrezzo fascia testuale proveniente dal database
     * @return simboli della fascia, stringa vuota per un valore assente oppure
     *         il valore originale se la fascia non è riconosciuta
     *
     * @author Michele Viselli
     */
    public static String formattaFasciaPrezzo(String fasciaPrezzo) {
        if (fasciaPrezzo == null || fasciaPrezzo.isBlank()) return "";

        String fasciaNormalizzata = fasciaPrezzo.trim();
        return switch (fasciaNormalizzata) {
            case "meno di 35 €" -> "€";
            case "tra 35 € e 60 €" -> "€€";
            case "tra 60 € e 100 €" -> "€€€";
            case "oltre 100 €" -> "€€€€";
            default -> fasciaNormalizzata;
        };
    }

    /**
     * Crea il badge con la media delle recensioni di un ristorante.
     * <p>
     * Il colore cambia in base al voto — verde dalle 4 stelle in su, giallo
     * fra le 2 e le 4, rosso sotto le 2 — ma il badge riporta sempre anche il
     * valore numerico e il numero di recensioni: l'informazione resta leggibile
     * anche a chi non distingue i colori.
     *
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

        // Le stesse tre soglie del riquadro della media nella dashboard:
        // un voto deve avere lo stesso colore ovunque compaia.
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
}
