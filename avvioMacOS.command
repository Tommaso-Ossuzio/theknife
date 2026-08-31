#!/bin/bash
cd "$(dirname "$0")"

SERVER="bin/serverTK-4.0.jar"
CLIENT="bin/clientTK-4.0.jar"
[ -f "$SERVER" ] || SERVER="serverTK/target/serverTK-4.0.jar"
[ -f "$CLIENT" ] || CLIENT="clientTK/target/clientTK-4.0.jar"
[ -f "$SERVER" ] || SERVER="serverTK-4.0.jar"
[ -f "$CLIENT" ] || CLIENT="clientTK-4.0.jar"

if ! command -v java >/dev/null 2>&1; then
    echo "Java non risulta installato o non e' presente nel PATH."
    echo "E' richiesta una versione compresa fra la 21 e la 25."
    exit 1
fi

if [ ! -f "$SERVER" ] || [ ! -f "$CLIENT" ]; then
    echo "Archivi dell'applicazione non trovati."
    echo "Sono attesi bin/serverTK-4.0.jar e bin/clientTK-4.0.jar,"
    echo "oppure il progetto compilato con: ./mvnw package"
    exit 1
fi

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

echo "Server pronto: avvio dell'applicazione."
java -jar "$CLIENT" &
