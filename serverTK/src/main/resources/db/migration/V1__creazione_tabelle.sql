CREATE TABLE NAZIONE (
    nome VARCHAR(100) NOT NULL,
    PRIMARY KEY (nome)
);

CREATE TABLE TIPO_CUCINA (
    nome VARCHAR(100) NOT NULL,
    PRIMARY KEY (nome)
);

CREATE TABLE COORDINATE (
    id SERIAL NOT NULL,
    latitudine DECIMAL(10, 8) NOT NULL,
    longitudine DECIMAL(11, 8) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CITTA (
    id_citta SERIAL NOT NULL,
    nome VARCHAR(100) NOT NULL,
    nome_nazione VARCHAR(100) NOT NULL REFERENCES NAZIONE(nome)
                                      ON UPDATE CASCADE
                                      ON DELETE CASCADE,
    PRIMARY KEY (id_citta)
);

CREATE TABLE LUOGO (
    id SERIAL NOT NULL,
    via VARCHAR(255) NOT NULL,
    id_citta INT NOT NULL REFERENCES CITTA(id_citta)
                          ON UPDATE CASCADE
                          ON DELETE CASCADE,
    id_coordinate INT NOT NULL UNIQUE REFERENCES COORDINATE(id)
                                      ON UPDATE CASCADE
                                      ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE UTENTE (
    id_utente SERIAL NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    data_nascita DATE,
    is_ristoratore BOOLEAN NOT NULL,
    id_luogo_vive INT NOT NULL REFERENCES LUOGO(id)
                               ON UPDATE CASCADE
                               ON DELETE CASCADE,
    PRIMARY KEY (id_utente)
);

CREATE TABLE RISTORANTE (
    nome VARCHAR(100) NOT NULL,
    telefono VARCHAR(50),
    sito_web VARCHAR(255),
    delivery BOOLEAN NOT NULL,
    prenotazione_online BOOLEAN NOT NULL,
    fascia_prezzo VARCHAR(50) NOT NULL,
    id_utente INT NOT NULL REFERENCES UTENTE(id_utente)
                           ON UPDATE CASCADE
                           ON DELETE CASCADE,
    id_luogo INT NOT NULL UNIQUE REFERENCES LUOGO(id)
                                 ON UPDATE CASCADE
                                 ON DELETE CASCADE,
    PRIMARY KEY (nome)
);

CREATE TABLE RECENSIONE (
    id SERIAL NOT NULL,
    data_ora TIMESTAMP NOT NULL,
    testo TEXT NOT NULL,
    numero_stelle INT NOT NULL CHECK (numero_stelle >= 1 AND numero_stelle <= 5),
    id_utente INT NOT NULL REFERENCES UTENTE(id_utente)
                           ON UPDATE CASCADE
                           ON DELETE CASCADE,
    nome_ristorante VARCHAR(100) NOT NULL REFERENCES RISTORANTE(nome)
                                          ON UPDATE CASCADE
                                          ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE RISPOSTA (
    id_recensione INT NOT NULL UNIQUE REFERENCES RECENSIONE(id)
                                      ON UPDATE CASCADE
                                      ON DELETE CASCADE,
    id_utente INT NOT NULL REFERENCES UTENTE(id_utente)
                           ON UPDATE CASCADE
                           ON DELETE CASCADE,
    testo_risposta TEXT NOT NULL,
    PRIMARY KEY (id_recensione)
);

CREATE TABLE PREFERITO (
    id_utente INT NOT NULL REFERENCES UTENTE(id_utente)
                           ON UPDATE CASCADE
                           ON DELETE CASCADE,
    nome_ristorante VARCHAR(100) NOT NULL REFERENCES RISTORANTE(nome)
                                          ON UPDATE CASCADE
                                          ON DELETE CASCADE,
    PRIMARY KEY (id_utente, nome_ristorante)
);

CREATE TABLE RISTORANTE_TIPO_CUCINA (
    nome_ristorante VARCHAR(100) NOT NULL REFERENCES RISTORANTE(nome)
                                          ON UPDATE CASCADE
                                          ON DELETE CASCADE,
    nome_tipo_cucina VARCHAR(100) NOT NULL REFERENCES TIPO_CUCINA(nome)
                                           ON UPDATE CASCADE
                                           ON DELETE CASCADE,
    PRIMARY KEY (nome_ristorante, nome_tipo_cucina)
);