#!/bin/bash
cd "$(dirname "$0")"

case "$(uname -s)" in
    Darwin)
        case "$(uname -m)" in
            arm64|aarch64) VARIANTE="-mac-aarch64" ;;
            *)             VARIANTE="-mac" ;;
        esac
        ;;
    *) VARIANTE="" ;;
esac

trovaArchivio() {
    for percorso in "bin/$1-4.0$VARIANTE.jar" "$2/target/$1-4.0$VARIANTE.jar" "$1-4.0$VARIANTE.jar"; do
        if [ -f "$percorso" ]; then
            echo "$percorso"
            return 0
        fi
    done
    return 1
}

SERVER="$(trovaArchivio serverTK serverTK)"
CLIENT="$(trovaArchivio clientTK clientTK)"

if ! command -v java >/dev/null 2>&1; then
    echo "Java non risulta installato o non e' presente nel PATH."
    echo "E' richiesta una versione compresa fra la 21 e la 25."
    exit 1
fi

if [ -z "$SERVER" ] || [ -z "$CLIENT" ]; then
    echo "Archivi dell'applicazione non trovati."
    echo "Sono attesi bin/serverTK-4.0$VARIANTE.jar e bin/clientTK-4.0$VARIANTE.jar."
    if [ "$VARIANTE" = "-mac" ]; then
        echo "Questo e' un Mac con processore Intel: gli archivi si generano con"
        echo "  ./mvnw package -Djavafx.platform=mac"
    elif [ "$VARIANTE" = "-mac-aarch64" ]; then
        echo "Questo e' un Mac con Apple Silicon: gli archivi si generano con"
        echo "  ./mvnw package -Djavafx.platform=mac-aarch64"
    else
        echo "Gli archivi si generano con: ./mvnw package"
    fi
    exit 1
fi

QUANTI=""
while [ -z "$QUANTI" ]; do
    printf "Quanti client vuoi aprire? [1-5, invio per 1]: "
    read -r risposta
    risposta=${risposta:-1}
    case "$risposta" in
        1|2|3|4|5) QUANTI="$risposta" ;;
        *) echo "Indicare un numero da 1 a 5." ;;
    esac
done

portaAperta() {
    # Non aprire una connessione di prova: il server usa ObjectInputStream e
    # interpretarebbe una connessione vuota come un client interrotto.
    if command -v lsof >/dev/null 2>&1; then
        lsof -nP -iTCP:8999 -sTCP:LISTEN 2>/dev/null |
            awk 'NR > 1 { trovato = 1 } END { exit(trovato ? 0 : 1) }'
        return $?
    fi

    if command -v ss >/dev/null 2>&1; then
        ss -ltn 2>/dev/null |
            awk '$4 ~ /(^|:)8999$/ { trovato = 1 } END { exit(trovato ? 0 : 1) }'
        return $?
    fi

    netstat -an 2>/dev/null |
        awk '$0 ~ /(^|[.:])8999[[:space:]].*LISTEN/ { trovato = 1 }
             END { exit(trovato ? 0 : 1) }'
}

echo
echo "Avvio del server TheKnife..."
java -jar "$SERVER" &

echo
echo "Indicare le credenziali di PostgreSQL nella finestra del server e premere Avvia server."
echo "Al primo avvio l'importazione del catalogo puo' richiedere qualche minuto."
echo
printf "Attesa del server sulla porta 8999"

pronto=""
for _ in $(seq 1 300); do
    if portaAperta; then
        pronto="si"
        break
    fi
    printf "."
    sleep 2
done
echo

if [ -z "$pronto" ]; then
    echo "Il server non risulta in ascolto sulla porta 8999."
    echo "Controllare i messaggi nella finestra del server e riprovare."
    exit 1
fi

if [ "$QUANTI" = "1" ]; then
    echo "Server pronto: avvio dell'applicazione."
else
    echo "Server pronto: avvio di $QUANTI client."
fi

for _ in $(seq 1 "$QUANTI"); do
    java -jar "$CLIENT" &
    sleep 1
done
