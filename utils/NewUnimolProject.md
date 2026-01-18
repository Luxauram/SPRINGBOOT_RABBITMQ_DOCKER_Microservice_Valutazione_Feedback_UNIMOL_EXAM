# Contesto Applicativo

Il progetto si colloca nel contesto della gestione delle attività didattiche all'interno di un
ambiente universitario.  
L'obiettivo è sviluppare una piattaforma software distribuita che faciliti l'interazione tra
studenti, docenti e l'amministrazione, migliorando:

- l'organizzazione dei corsi
- la condivisione del materiale didattico
- la gestione delle valutazioni
- la comunicazione

La piattaforma mira a digitalizzare e centralizzare diverse attività accademiche, rendendo i
processi più efficienti e accessibili.

---

# Requisiti del Sistema

Il sistema dovrà fornire le seguenti funzionalità:

## Gestione Corsi

### _(Responsabile della memorizzazione e della gestione delle informazioni relative ai corsi (

dettagli, docenti, orari))_

[Gestito da Marco](https://github.com/m-gianfagna/Microservizio-Gestione-Corsi)

- **(Amministrativi)** Creazione, modifica ed eliminazione di corsi. Ogni corso è associato a uno o
  più docenti.
- **(Tutti)** Visualizzazione dell'elenco dei corsi disponibili.
- **(Tutti)** Visualizzazione dei dettagli di un corso (nome, codice, descrizione, crediti, docenti,
  orari).

## Gestione Materiale Didattico

### _(Responsabile dell'archiviazione e il recupero del materiale didattico associato ai corsi)_

[Gestito da Lorenzo]()

- **(Docenti)** Caricamento di materiale didattico associato ai corsi.
- **(Studenti e Docenti)** Visualizzazione e download del materiale didattico per i corsi a cui si è
  iscritti.
- **(Docenti)** Organizzazione del materiale didattico per corso.

## Gestione Compiti

### _(Responsabile della gestione dei compiti assegnati, delle consegne degli studenti e dello stato

di valutazione)_

[Gestito da Vittorio](https://github.com/VittorioDiPalma/Progetto-Gestione-Compiti )

- **(Docenti)** Assegnazione di nuovi compiti (con titolo, descrizione, data di scadenza, allegati).
- **(Studenti)** Visualizzazione dei compiti assegnati per i corsi a cui si è iscritti.
- **(Studenti)** Consegna dei compiti (upload di file).
- **(Docenti)** Visualizzazione dello stato dei compiti (assegnato, consegnato, valutato).
- **(Docenti)** Visualizzazione delle consegne degli studenti.

## Gestione Esami

### _(Responsabile per la pianificazione degli esami, delle iscrizioni degli studenti e della

registrazione dei voti)_

[Gestito da Luca](https://github.com/Lucalanese/MicroServiziEsame)

- **(Tutti)** Visualizzazione del calendario degli esami (data, ora, aula, corso).
- **(Studenti)** Iscrizione agli esami.
- **(Docenti)** Registrazione dei voti degli esami.
- **(Studenti)** Visualizzazione dei risultati degli esami.
- **(Amministrativi)** Pianificazione degli esami.

## Gestione Utenti e Ruoli

### _(Responsabile dell'autenticazione, dell'autorizzazione e della gestione delle informazioni

degli utenti e dei loro ruoli all'interno del sistema)_

[Gestito da Mauro](https://github.com/Maurocavasinni/Gestione-Utenti-e-Ruoli)

- **(Amministrativi)** Registrazione di nuovi utenti (di tipo docente, studente o amministrativo).
- **(Tutti)** Autenticazione (login) degli utenti.
- **(Tutti)** Gestione dei profili utente.
- **(Amministrativi)** Gestione dei ruoli e dei permessi per l'accesso alle funzionalità.

## Comunicazioni e Notifiche

### _(Responsabile della gestione di invio e ricezione di messaggi tra utenti e dell'invio di

notifiche relative alle attività accademiche)_

[Gestito da Marcello](https://github.com/MarcelloPastore/Progetto-Microservizi-e-Gestione-API)

- **(Studenti e Docenti)** Invio di messaggi tra studenti e docenti all'interno del contesto dei
  corsi.
- Notifiche relative a nuove attività (compiti, materiale, esami).

## Gestione delle Presenze

### _(Responsabile della registrazione e della gestione dei dati relativi alle presenze degli

studenti alle lezioni)_

[Gestito da Luigi](https://github.com/giggi30/Gestione-Presenze.git)

- **(Docenti)** Registrazione delle presenze degli studenti.
- **(Studenti)** Visualizzazione del registro delle presenze.

## Valutazione e Feedback

### _(Responsabile dell'aggiunta e della visualizzazione del feedback fornito dai docenti sui

compiti e sugli esami)_

[Gestito da me (LuxAuram)](https://github.com/Luxauram/SPRINGBOOT-UNIMOL-MS-Valutazione-Feedback)

- **(Docenti)** Fornitura di feedback dettagliato sui compiti e sugli esami.
- **(Studenti)** Visualizzazione del feedback ricevuto.
- **(Amministrativi)** Creazione di un questionario di feedback sui docenti.
- **(Studenti)** Compilazione del questionario di feedback sui docenti.

## Pianificazione Orari e Aule

### _(Responsabile della gestione delle informazioni sulle aule e della pianificazione degli orari

delle lezioni e degli esami)_

[Gestito da Simone](https://github.com/Simo-2004/Progetto-Microservizi-UNIMOL)

- **(Tutti)** Visualizzazione degli orari delle lezioni e degli esami.
- **(Amministrativi)** Gestione della disponibilità delle aule e pianificazione degli orari.

## Gestione delle Iscrizioni ai Corsi

### _(Responsabile della gestione delle richieste di iscrizione degli studenti ai corsi e il

processo di approvazione)_

[Gestito da Antonio](https://github.com/antoniods10/Gestione-delle-iscrizioni-ai-corsi.git)

- Gestione delle richieste di iscrizione degli studenti ai corsi e del processo di approvazione.
- Fornitura di API per la richiesta e la gestione delle iscrizioni.

## Analisi e Reportistica

### _(Responsabile della generazione di report di base sull'attività della piattaforma)_

[Gestito da Francesco](https://github.com/f-ferretti/Analisi-Reportistica)

- **(Amministrativi)** Generazione di report sulle attività di un singolo studente.
- **(Amministrativi)** Generazione di report sulle performance degli studenti in un dato corso.
- **(Amministrativi)** Generazione di report sulle valutazioni di un docente.

## Gestione dei Pagamenti e delle Tasse Universitarie

### _(Responsabile della gestione delle informazioni relative alle tasse universitarie, le scadenze,

i metodi di pagamento e lo stato dei pagamenti degli studenti)_

[Gestito da Donato]()

- **(Amministrativi)** Gestione delle soglie ISEE per la determinazione delle tasse degli studenti
- **(Amministrativi)** Generazione degli avvisi di pagamento per tutti gli studenti (in PDF)
- **(Studenti)** Pagamento delle tasse (simulando un pagamento con PagoPA)

## Supporto ed Help Desk

### _(Responsabile della gestione delle richieste di supporto da parte di studenti e docenti)_

[Gestito da Alessio](https://github.com/alessiodlm/Unimol-Progetto-Microservizi)

- **(Studenti e Docenti)** Apertura di ticket di supporto in base a un'area tematica (es: tasse,
  biblioteca) e interazione con chi gestirà il ticket
- **(Amministrativi)** Gestione del ticket, considerando diversi possibili stati, ovvero: "preso in
  carico" (assegnato a un operatore); "in lavorazione" (l'operatore ha iniziato ad analizzare la
  problematica); "informazioni necessarie" (solo qualora fosse necessario che chi ha aperto il
  ticket fornisca ulteriori informazioni); "risolto" (se il problema è risolto); "chiuso" (se il
  problema non può essere risolto).

## Gestione Biblioteca

### _(Responsabile della gestione dell'acquisizione di libri e dei prestiti)_

[Gestito da Davide](https://github.com/Davide-del-Villano/Gestione-Bilioteca)

- **(Amministrativi)** Gestione dei dati bibliografici dei libri in biblioteca
- **(Amministrativi)** Gestione dell'acquisizione e rimozione di copie di un determinato libro
- **(Studenti)** Richiesta di prestito
- **(Amministrativi)** Gestione di prestiti e restituzioni
