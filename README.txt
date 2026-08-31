====================================================================
 THEKNIFE
 Trova, recensisci e gestisci ristoranti.
 Laboratorio Interdisciplinare A
 Universita' degli Studi dell'Insubria - sede di Varese
====================================================================

INDICE
  1. Panoramica
  2. Funzionalita'
  3. Architettura
  4. Requisiti
  5. Installazione
  6. Esecuzione
  7. Struttura del repository
  8. Documentazione
  9. Autori


1. PANORAMICA
--------------------------------------------------------------------
TheKnife e' una piattaforma che permette di cercare ristoranti in
tutto il mondo e di filtrarli per citta', tipo di cucina, fascia di
prezzo, valutazione media, consegna a domicilio e prenotazione in
linea. I clienti registrati possono salvare i preferiti e scrivere
recensioni; i ristoratori inseriscono i propri locali, seguono
l'andamento delle valutazioni e rispondono ai giudizi ricevuti.

Il catalogo di partenza e' quello della Guida Michelin: circa 17.700
ristoranti, importati automaticamente in un database PostgreSQL al
primo avvio del server.

Le immagini dell'applicazione sono riportate nel file README.md e
nella cartella doc/img.


2. FUNZIONALITA'
--------------------------------------------------------------------
Ospite
  - ricerca per citta' e filtri avanzati
  - scheda del ristorante con il sito web integrato
  - lettura delle recensioni

Cliente (comprende quanto sopra)
  - preferiti
  - scrittura, modifica ed eliminazione delle proprie recensioni

Ristoratore (comprende le funzioni di ricerca e consultazione)
  - inserimento di nuovi ristoranti
  - riepilogo dell'attivita' e risposta alle recensioni ricevute

Il ruolo viene scelto in fase di registrazione ed e' esclusivo.
L'interfaccia e' disponibile in tema chiaro e in tema scuro, con i
contrasti verificati secondo le linee guida WCAG.


3. ARCHITETTURA
--------------------------------------------------------------------
Il progetto e' un applicativo Maven multi-modulo diviso in tre parti:

  clientTK   interfaccia grafica JavaFX: viste FXML, controller,
             gestione della sessione e del tema
  serverTK   server multi-thread: protocollo su socket, DAO JDBC,
             creazione dello schema e importazione dei dati
  commonTK   DTO serializzabili scambiati fra i due lati e regole di
             validazione condivise

Il client non accede mai al database: apre una connessione TCP verso
il server sulla porta 8999 e gli inoltra ogni ricerca, recensione o
salvataggio. Il server accoglie ogni client con un thread dedicato,
quindi piu' utenti possono lavorare contemporaneamente. Lo schema del
database e' versionato con Flyway e viene creato al primo avvio.


4. REQUISITI
--------------------------------------------------------------------
  Java (JRE o JDK)   da 21 a 25 (la 21 e' la versione di riferimento)
  PostgreSQL         12 o successiva (collaudato sulla 18)
  Sistema operativo  Windows 10/11, macOS, Linux
  Memoria            almeno 256 MB liberi

IMPORTANTE: non utilizzare Java 26 o versioni successive. Il
componente WebView di JavaFX, usato per mostrare il sito dei
ristoranti, richiede il modulo jdk.jsobject che da Java 26 non esiste
piu' e l'applicazione non si avvia.


5. INSTALLAZIONE
--------------------------------------------------------------------
Pacchetto gia' compilato
  1. scaricare ed estrarre l'archivio .zip distribuito dagli autori;
  2. verificare che nella cartella bin/ siano presenti gli archivi per
     il proprio sistema: serverTK-4.0.jar e clientTK-4.0.jar per
     Windows, serverTK-4.0-mac-aarch64.jar e
     clientTK-4.0-mac-aarch64.jar per macOS;
  3. assicurarsi che il servizio PostgreSQL sia in esecuzione.

  Nota per macOS: gli archivi con il suffisso -mac-aarch64 contengono
  le librerie native di JavaFX in formato .dylib e girano sui Mac con
  Apple Silicon. Per un Mac con processore Intel si rigenerano con
  ./mvnw package -Djavafx.platform=mac

Compilazione dal sorgente
     git clone https://github.com/sonoFrangu/theknife.git
     cd theknife
     ./mvnw package

I due archivi eseguibili vengono creati in
serverTK/target/serverTK-4.0.jar e clientTK/target/clientTK-4.0.jar.
Su Windows si usa mvnw.cmd al posto di ./mvnw.


6. ESECUZIONE
--------------------------------------------------------------------
Il server va sempre avviato per primo: il client, all'apertura,
chiede al server l'elenco delle citta' e senza di esso non e' in
grado di mostrare alcun dato.

Avvio rapido
  Gli script nella cartella principale chiedono quante finestre
  dell'applicazione aprire (da 1 a 5, una se si preme invio),
  avviano il server, attendono che sia in ascolto e aprono da soli
  i client.

    Windows          doppio clic su avvioWindows.bat
    macOS e Linux    chmod +x avvioMacOS.command (una sola volta),
                     poi doppio clic su avvioMacOS.command

  Gli script cercano gli archivi prima in bin/, poi nelle cartelle
  target/ prodotte dalla compilazione: funzionano quindi sia con il
  pacchetto distribuito sia con il progetto appena compilato.

Avvio manuale
    java -jar bin/serverTK-4.0.jar
    (dopo che il registro riporta "Server in ascolto sulla porta
    8999")
    java -jar bin/clientTK-4.0.jar

  Su macOS gli archivi da lanciare sono
  bin/serverTK-4.0-mac-aarch64.jar e bin/clientTK-4.0-mac-aarch64.jar.

  Su Windows si puo' usare javaw -jar per non lasciare aperta la
  finestra del terminale.

Primo avvio
  Nella finestra del server vanno indicati indirizzo, porta, utente e
  password di PostgreSQL; i campi sono gia' compilati con i valori
  piu' comuni (localhost, 5432, postgres). Alla prima esecuzione il
  server crea da solo il database theknife_db, genera le tabelle e
  importa il catalogo: questa fase puo' richiedere qualche minuto.
  Gli avvii successivi riconoscono che i dati ci sono gia' e sono
  immediati.


7. STRUTTURA DEL REPOSITORY
--------------------------------------------------------------------
  clientTK/            applicazione JavaFX (viste FXML, controller,
                       foglio di stile)
  serverTK/            server socket, DAO, migrazioni Flyway e
                       importazione dei dati
  commonTK/            DTO condivisi fra client e server
  bin/                 archivi eseguibili del pacchetto distribuito
  doc/                 manuali in PDF, diagrammi UML ed ER, Javadoc
  avvioWindows.bat
  avvioMacOS.command
  pom.xml              POM padre del progetto multi-modulo


8. DOCUMENTAZIONE
--------------------------------------------------------------------
  - Manuale Utente e Manuale Tecnico in formato PDF nella cartella
    doc/
  - Javadoc dei tre moduli in doc/javadoc, rigenerabile con
    ./mvnw javadoc:aggregate
  - diagramma ER e diagrammi UML nella cartella doc/


9. AUTORI
--------------------------------------------------------------------
  Matteo Franguelli      matricola 761133   Varese
  Elia Toschi            matricola 760873   Varese
  Celestino Resteghini   matricola 760865   Varese
  Michele Viselli        matricola 763016   Varese

Progetto realizzato per il corso di Laboratorio Interdisciplinare A,
anno accademico 2025/2026.
