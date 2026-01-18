# Microservizio Valutazione e Feedback

## Indice

1. [Panoramica](#panoramica)
2. [Tech Stack](#tech-stack)
3. [Modello Dati](#modello-dati)
    - [DTO](#dto)
        - [Valutazione](#dto-per-la-valutazione)
        - [Feedback Dettagliato](#dto-per-il-feedback-dettagliato)
        - [Questionario Docente](#dto-per-il-questionario-docente)
        - [Risposta Questionario](#dto-per-risposta-questionario)
    - [Entità Princiapli JPA](#entità-principali-jpa)
        - [Assessment (Valutazione)](#assessment-valutazione)
        - [DetailedFeedback (Feedback Dettagliato)](#detailedfeedback-feedback-dettagliato)
        - [TeacherSurvey (Questionario Docente)](#teachersurvey-questionario-docente)
        - [SurveyResponse (Risposta Questionario)](#surveyresponse-risposta-questionario)
4. [API REST](#api-rest)
    - [Assessments Endpoint](#assessments-endpoint)
    - [Detailed Feedback Endpoint](#detailed-feedback-endpoint)
    - [Teacher Surveys Endpoint](#teacher-surveys-endpoint)
    - [Surveys Response Endpoint](#surveys-response-endpoint)
5. [Integrazione Microservizi Esterni](#integrazione-microservizi-esterni)
    - [Panoramica Generale](#panoramica-generale)
    - [RabbitMQ - Published Events](#rabbitmq---published-events)
    - [RabbitMQ - Consumed Events](#rabbitmq---consumed-events)
6. [Sicurezza e Autorizzazioni](#sicurezza-e-autorizzazioni)
7. [Come Farlo Funzionare sui Vostri OS](#come-farlo-funzionare-sui-vostri-os)
    - [1. Avvio del Microservizio con Docker Compose](#1-avvio-del-microservizio-con-docker-compose)
    - [2. Recupero password di Spring Security](#2-recupero-password-di-spring-security-non-necessario-ma-potrebbe-servire)
    - [3. Accesso alle Interfacce Web](#3-accesso-alle-interfacce-web)
    - [4. Testing con Postman](#4-testing-con-postman)
8. [EXTRA](#extra)
    - [API-Gateway](#api-gateway)
    - [Gestione Utenti e Ruoli - Mauro](#gestione-utenti-e-ruoli---mauro)

---

## Panoramica

### Traccia Completa

La traccia completa è presente [qui:./utils/NewUnimolProject.md](./utils/NewUnimolProject.md)

### La mia parte di Traccia - Valutazione e Feedback

Questo Microservizio è responsabile dell'aggiunta e della visualizzazione del feedback fornito dai
docenti sui compiti e sugli esami:

- **(Docenti)** Fornitura di feedback dettagliato sui compiti e sugli esami.
- **(Studenti)** Visualizzazione del feedback ricevuto.
- **(Amministrativi)** Creazione di un questionario di feedback sui docenti
- **(Studenti)** Compilazione del questionario di feedback sui docenti

## Tech Stack

- **Framework**: SpringBoot
- **Message Broker**: RabbitMQ
- **Database**: PostgreSQL
- **Containerization**: Docker
- **Orchestration**: Kubernetes (non presente su questa repo)
- **API Documentation**: Swagger/OpenAPI 3.0

## Modello Dati

### DTO

_DataTransferObject presenti nel microservizio._

#### DTO per la Valutazione

```java
public class AssessmentDTO {

  private String id;
  private String referenceId;
  private ReferenceType referenceType;
  private String studentId;
  private String teacherId;
  private Double score;
  private LocalDateTime assessmentDate;
  private String notes;
  private String courseId;
}
```

#### DTO per il Feedback Dettagliato

```java
public class DetailedFeedbackDTO {

  private String id;
  private String assessmentId;
  private String feedbackText;
  private FeedbackCategory category;
  private String strengths;
  private String improvementAreas;
}
```

#### DTO per il Questionario Docente

```java
public class TeacherSurveyDTO {

  private String id;
  private String courseId;
  private String teacherId;
  private String academicYear;
  private Integer semester;
  private SurveyStatus status;
  private LocalDateTime creationDate;
  private LocalDateTime closingDate;
  private String title;
  private String description;
  private List<SurveyQuestionDTO> questions;
}
```

```java
public static class SurveyQuestionDTO {

  private String id;
  private String questionText;
  private QuestionType questionType;
  private Integer minRating;
  private Integer maxRating;
  private Integer maxLengthText;
}
```

#### DTO per Risposta Questionario

```java
public class SurveyResponseDTO {

  private String id;
  private String surveyId;
  private String studentId;
  private String questionId;
  private Integer numericRating;
  private String textComment;
  private LocalDateTime submissionDate;
}
```

---

### Entità principali JPA

*Tabelle in PostgreSQL per strutture dati del microservizio*

#### Assessment *(Valutazione)*

- `id` - ID valutazione
- `reference_id` - ID riferimento (compito o esame)
- `reference_type` - Tipo riferimento (enum: ASSIGNMENT, EXAM)
- `student_id` - ID studente
- `teacher_id` - ID docente
- `score` - Punteggio/voto
- `assessment_date` - Data valutazione
- `notes` - Note/commenti generali
- `course_Id` - ID riferimento del corso
- `created_at` - Timestamp creazione
- `updated_at` - Timestamp aggiornamento

#### DetailedFeedback *(Feedback Dettagliato)*

- `id` - ID feedback
- `assessment_id` - ID valutazione (riferimento)
- `feedback_text` - Testo feedback
- `category` - Categoria feedback (enum: CONTENT, PRESENTATION, CORRECTNESS, OTHER)
- `strengths` - Punti di forza (testo)
- `improvement_areas` - Aree di miglioramento (testo)
- `created_at` - Timestamp creazione
- `updated_at` - Timestamp aggiornamento

#### TeacherSurvey *(Questionario Docente)*

- `id` - ID questionario
- `course_id` - ID corso
- `teacher_id` - ID docente
- `academic_year` - Anno accademico
- `semester` - Semestre
- `status` - Stato (enum: ACTIVE, CLOSED)
- `creation_date` - Data creazione
- `closing_date` - Data chiusura
- `created_at` - Timestamp creazione
- `updated_at` - Timestamp aggiornamento
- `title` - Titolo questionario
- `description` - Descrizione questionario
- `questions` - Domande questionario

#### SurveyResponse *(Risposta Questionario)*

- `id` - ID risposta
- `survey_id` - ID questionario
- `student_id` - ID studente (opzionale, anonimizzato)
- `question_id` - Domanda (reference a catalogo domande)
- `numeric_rating` - Valutazione numerica (1-5)
- `text_comment` - Commento testuale (opzionale)
- `submission_date` - Data compilazione
- `created_at` - Timestamp creazione
- `updated_at` - Timestamp aggiornamento

## API REST

### Assessments Endpoint

```bash
######### Gestione Valutazioni (per Docenti) #########

#############################################
# Lista valutazioni
# @func: getAllAssessments()
# @param: none
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments

#############################################
# Dettaglio singola valutazione
# @func: getAssessmentById()
# @param: String id
# @return: ResponseEntity<AssessmentDTO>
#############################################
GET     /api/v1/assessments/{id}

#############################################
# Valutazioni per un compito
# @func: getAssessmentsByAssignment()
# @param: String id
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments/assignment/{id}

#############################################
# Valutazioni per un esame
# @func: getAssessmentsByExam()
# @param: String id
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments/exam/{id}

#############################################
# Valutazioni per uno studente
# @func: getAssessmentsByStudent()
# @param: String id
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments/student/{id}

#############################################
# Tutte le valutazioni per un corso
# @func: getAssessmentsByCourse()
# @param: String id
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments/course/{id}

#############################################
# Valutazioni personali
# @func: getPersonalAssessments()
# @param: HttpServletRequest request
# @return: ResponseEntity<List<AssessmentDTO>>
#############################################
GET     /api/v1/assessments/personal

#############################################
# Dettaglio valutazione personale
# @func: getPersonalAssessmentDetails()
# @param: String id
# @return: ResponseEntity<AssessmentDTO>
#############################################
GET     /api/v1/assessments/personal/{id}

#############################################
# Crea nuova valutazione
# @func: createAssessment()
# @param: AssessmentDTO assessmentDTO
# @param: HttpServletRequest request
# @return: ResponseEntity<AssessmentDTO>
#############################################
POST    /api/v1/assessments

#############################################
# Aggiorna valutazione
# @func: updateAssessment()
# @param: String id
# @param: AssessmentDTO assessmentDTO
# @return: ResponseEntity<AssessmentDTO>
#############################################
PUT     /api/v1/assessments/{id}

#############################################
# Elimina valutazione
# @func: deleteAssessment()
# @param: String id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/assessments/{id}
```

---

### Detailed Feedback Endpoint

```bash
#############################################
# Lista di tutti i feedback
# @func: getAllFeedback()
# @param: none
# @return: ResponseEntity<List<DetailedFeedbackDTO>>
#############################################
GET     /api/v1/feedback

#############################################
# Feedback per una valutazione
# @func: getFeedbackByAssessmentId()
# @param: String id
# @return: ResponseEntity<List<DetailedFeedbackDTO>>
#############################################
GET     /api/v1/feedback/assessment/{id}

#############################################
# Dettaglio singolo feedback
# @func: getFeedbackById()
# @param: String id
# @return: ResponseEntity<DetailedFeedbackDTO>
#############################################
GET     /api/v1/feedback/{id}

#############################################
# Feedback personali dello studente autenticato
# @func: getPersonalFeedback()
# @param: HttpServletRequest request
# @return: ResponseEntity<List<DetailedFeedbackDTO>>
#############################################
GET     /api/v1/feedback/personal

#############################################
# Crea nuovo feedback
# @func: createFeedback()
# @param: DetailedFeedbackDTO feedbackDTO
# @param: HttpServletRequest request
# @return: ResponseEntity<DetailedFeedbackDTO>
#############################################
POST    /api/v1/feedback

#############################################
# Aggiorna singolo feedback
# @func: updateFeedback()
# @param: String id
# @param: DetailedFeedbackDTO feedbackDTO
# @return: ResponseEntity<DetailedFeedbackDTO>
#############################################
PUT     /api/v1/feedback/{id}

#############################################
# Elimina singolo feedback
# @func: deleteFeedback()
# @param: String id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/feedback/{id}
```

---

### Teacher Surveys Endpoint

```bash
#############################################
# Lista questionari
# @func: getAllSurveys()
# @param: none
# @return: ResponseEntity<List<TeacherSurveyDTO>>
#############################################
GET     /api/v1/teacher-surveys

#############################################
# Dettaglio questionario
# @func: getSurveyById()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<TeacherSurveyDTO>
#############################################
GET     /api/v1/teacher-surveys/{id}

#############################################
# Questionari per corso
# @func: getSurveysByCourse()
# @param: String courseId
# @return: ResponseEntity<List<TeacherSurveyDTO>>
#############################################
GET     /api/v1/teacher-surveys/course/{courseId}

#############################################
# Questionari per docente
# @func: getSurveysByTeacher()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<List<TeacherSurveyDTO>>
#############################################
GET     /api/v1/surveys/teacher/{id}

#############################################
# Questionari attivi
# @func: getActiveSurveys()
# @param: none
# @return: ResponseEntity<List<TeacherSurveyDTO>>
#############################################
GET     /api/v1/teacher-surveys/active


#############################################
# Ottieni risultati questionario
# @func: getSurveyResults()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<Object>
#############################################
GET     /api/v1/teacher-surveys/{id}/results

#############################################
# Crea nuovo questionario
# @func: createSurvey()
# @param: TeacherSurveyDTO surveyDTO
# @return: ResponseEntity<TeacherSurveyDTO>
#############################################
POST    /api/v1/teacher-surveys

#############################################
# Aggiorna questionario
# @func: updateSurvey()
# @param: String id
# @param: TeacherSurveyDTO surveyDTO
# @return: ResponseEntity<TeacherSurveyDTO>
#############################################
PUT     /api/v1/teacher-surveys/{id}

#############################################
# Modifica stato questionario
# @func: changeSurveyStatus()
# @param: String id
# @param: SurveyStatus status
# @return: ResponseEntity<TeacherSurveyDTO>
#############################################
PUT     /api/v1/teacher-surveys/{id}/status

#############################################
# Elimina questionario
# @func: deleteSurvey()
# @param: String id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/teacher-surveys/{id}

#############################################
# Ottieni statistiche generali
# @func: getGeneralStatistics()
# @param: none
# @return: ResponseEntity<Object>
#############################################
GET     /api/v1/teacher-surveys/statistics
```

---

### Surveys Response Endpoint

```bash
#############################################
# Recupera risposte complete
# @func: getResponsesBySurveyId()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<List<SurveyResponseDTO>>
#############################################
GET     /api/v1/surveys/{id}/responses

#############################################
# Commenti testuali questionario
# @func: getSurveyComments()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<List<SurveyResponseDTO>>
#############################################
GET     /api/v1/surveys/{id}/comments
 
#############################################
# Risultati aggregati questionario
# @func: getSurveyResults()
# @param: String id
# @param: HttpServletRequest request
# @return: ResponseEntity<Map<String, Double>>
#############################################
GET     /api/v1/surveys/{id}/results

#############################################
# Invia risposte questionario
# @func: submitSurveyResponses()
# @param: String surveyId
# @param: List<SurveyResponseDTO> responseDTOs
# @param: HttpServletRequest request
# @return: ResponseEntity<List<SurveyResponseDTO>>
#############################################
POST    /api/v1/surveys/{surveyId}/responses

#############################################
# Ottieni le mie risposte ai questionari
# @func: getMyResponses()
# @param: HttpServletRequest request
# @return: ResponseEntity<List<SurveyResponseDTO>>
#############################################
GET     /api/v1/surveys/my-responses


#############################################
# Ottieni questionari disponibili
# @func: getAvailableSurveys()
# @param: HttpServletRequest request
# @return: ResponseEntity<?>
#############################################
GET     /api/v1/surveys/available
```

---

## Integrazione Microservizi Esterni

### Panoramica Generale

Il microservizio **Valutazione e Feedback** interagisce con i seguenti microservizi:

- **Gestione Utenti e Ruoli**: Per verificare autorizzazioni e ottenere informazioni su studenti e
  docenti
- **Gestione Compiti**: Per ottenere dettagli sui compiti da valutare
- **Gestione Esami**: Per ottenere dettagli sugli esami da valutare
- **Gestione Corsi**: Per ottenere informazioni sui corsi associati alle valutazioni

---

### RabbitMQ - Publisher Events

#### Assessment Queues

- `rabbitmq.queue.assessment.created`: Quando viene creata una nuova valutazione
- `rabbitmq.queue.assessment.updated`: Quando viene aggiornata una nuova valutazione
- `rabbitmq.queue.assessment.deleted`: Quando viene eliminata una nuova valutazione

#### Feedback Queues

- `rabbitmq.queue.feedback.create`: Quando viene creato un nuovo feedback dettagliato
- `rabbitmq.queue.feedback.updated`: Quando viene aggiornato un nuovo feedback dettagliato
- `rabbitmq.queue.feedback.deleted`: Quando viene eliminato un nuovo feedback dettagliato

#### Teacher Survey Queues

- `rabbitmq.queue.survey.completed`: Quando viene completato un questionario

#### Survey Response Queues

- `rabbitmq.queue.survey.response.submitted`: Quando viene inviato un questionario

---

### RabbitMQ - Consumer Events

#### Assignment Queues - Da Gestione Compiti (Vittorio)

- `rabbitmq.queue.assignmentSubmitted`: Quando uno studente invia un compito

#### Exam Queues - Da Gestione Esami (Luca)

- `rabbitmq.queue.examCompleted`: Quando uno studente completa un esame
- `rabbitmq.queue.examGradeRegistered`: Quando viene registrato un voto per un esame

#### Course Management Queues - Da Gestione Corsi (Marco)

- `rabbitmq.queue.courseCreated`: Quando viene creato un nuovo corso
- `rabbitmq.queue.courseDeleted`: Quando viene eliminato un corso esistente

#### User Management Queues - Da Gestione Utenti (Mauro)

- `rabbitmq.queue.teacherCreated`: Quando viene creato un nuovo account per un docente
- `rabbitmq.queue.studentCreated`:Quando viene creato un nuovo account per uno studente

---

## Sicurezza e Autorizzazioni

L'accesso alle API è regolato da autorizzazioni basate sui seguenti ROLE_TYPE:

- **STUDENTS**: Può visualizzare solo le proprie valutazioni e compilare questionari
- **TEACHER**: Può creare/modificare valutazioni e feedback solo per i propri corsi
- **ADMIN**: Amministrativo in grado di fare ogni cosa
- **SUPER_ADMIN**: Super Amministrativo in grado di fare ogni cosa (Utente Root)

---

## Come Farlo Funzionare sui Vostri OS

Cosa importantissima che viene prima di tutto il resto: va creato un file `.env` nella root del
progetto dove vanno inserite le **PROPRIE** variabili d'ambiente.

Di base viene impostato un file d'esempio (.env.example) nel quale vanno inserite queste variabili e
poi il file va rinominato in `.env`.

In questo caso particolare, si può
trovare [qui: ./utils/this-should-not-exist.txt](./utils/this-should-not-exist.txt) il file
`this-should-not-exist.txt` che contiene la copia originale del file env usato per lo sviluppo. Per
questo ambiente didattico diciamo che "va bene così".

Copiare il contenuto e incollarlo su .env.example (ricordandosi di rinominarlo in `.env`) oppure
prendere `this-should-not-exist.txt`, spostarlo nella root e rinominarlo `.env`.

Adesso occorre aprire un terminale nella root del progetto ed eseguire:

```bash
mvn clean install
```

oppure se non si ha maven installato globalmente:

```bash
# Su Linux o macOS
./mvnw clean install

# Su Windows (Command Prompt o PowerShell):
mvnw.cmd clean install
```

### 1. Avvio del Microservizio con Docker Compose

Per far partire il microservizio (e tutti il resto contenuto nella directory) bisogna:

1. **Assicurarsi che Docker sia installato.**
2. **Assicurarsi che Docker sia in esecuzione.**
2. **Aprire il terminale** nella root del progetto (dove si trova il file `docker-compose.yml`).
3. **Eseguire il seguente comando**:
   ```bash
   docker compose up --build
   ```

Questo avvierà tutti i servizi definiti nel `docker-compose.yml` (e (ri)costruirà le immagini Docker
se necessario).

---

### 2. Recupero password di Spring Security [NON NECESSARIO MA POTREBBE SERVIRE]

Per accedere agli endpoint Actuator del microservizio di Valutazione e Feedback, sono protetti da
Spring Security, ci sarà
bisogno della password generata dinamicamente da SrpingBoot, ecco come recuperarla:

1. **Visualizzare i container Docker in esecuzione**:
   Aprire nuovo terminale e digitare:
   ```bash
   docker compose ps
   ```
   Questo comando elencherà tutti i servizi e i relativi ID dei container in esecuzione.

2. **Identificare l'ID (o il nome) del container del microservizio**:
   Trova la riga relativa al tuo microservizio (es. `unimol-microservice-assessment-feedback`) e
   annotare **`ID`** (una stringa alfanumerica) o usare direttamente il nome del container.

3. **Recuperare i log del container**:
   Utilizzare l'ID (o il nome) del container trovato:
   ```bash
   docker logs <ID_DEL_CONTAINER>
   ```

4. **Cercare la password nei log**:
   Scorrendo tra il log generati si potrà scorgere qualcosa del tipo:
   ```
   Using generated security password: <PASSWORD_GENERATA>
   ```
   **Questa sarà la password** da usare con l'username **`user`** per accedere agli endpoint
   Actuator del microservizio.

---

### 3. Accesso alle Interfacce Web

Una volta che il microservizio è in esecuzione, si potrà accedere alle seguenti interfacce web:

* **RabbitMQ Management Console**:
    * **URL**: [http://localhost:15672](http://localhost:15672)
    * **Descrizione**: Interfaccia di gestione per monitorare e interagire con le code di messaggi
      di RabbitMQ.

* **Swagger UI - API-Gateway**:
    * **URL
      **: [http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)
    * **Descrizione**: Documentazione interattiva delle API per l'api-gateway.

* **Open API Docs - API-Gateway**:
    * **URL
      **: [http://localhost:8080/v3/api-docs](http://localhost:8080/webjars/swagger-ui/index.html)
    * **Descrizione**: Visualizzazione JSON delle API per l'api-gateway.

* **Swagger UI - Microservizio User Role**:
    * **URL
      **: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
    * **Descrizione**: Documentazione interattiva delle API per il microservizio di gestione utente
      e ruoli.

* **Open API Docs - Microservizio User Role**:
    * **URL**: [http://localhost:8081/v3/api-docs](http://localhost:8081/swagger-ui/index.html)
    * **Descrizione**: Visualizzazione JSON delle API per il microservizio di gestione utente e
      ruoli.

* **Swagger UI - Microservizio Assessment Feedback**:
    * **URL
      **: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html])
    * **Descrizione**: Documentazione interattiva delle API per il microservizio di gestione
      valutazioni e feedback.

* **Open API Docs - Microservizio Assessment Feedback**:
    * **URL**: [http://localhost:8082/v3/api-docs](http://localhost:8082/swagger-ui/index.html])
    * **Descrizione**: Visualizzazione JSON delle API per il microservizio di gestione valutazioni e
      feedback.

---

### 4. Testing con Postman

Una volta che il microservizio è in esecuzione, si potrà inoltre testare con Postman:

1. *
   *[Cliccando Qui: ./utils/New_Unimol_APIs.postman_collection.json](./utils/New_Unimol_APIs.postman_collection.json)
   ** sarà possibile arrivare al file json con tutte le mie API già pronte per essere testate, ma
   occorrerà anche *
   *[cliccare qui: ./utils/workspace.postman_globals.json](./utils/workspace.postman_globals.json)**
   per trovare il file con le variabili globali usate in Postman:
2. **Aprire Postman.**
3. **Creare un nuovo Workspace (molto consigliato ma non per forza necessario).**
4. **In alto a sinistra** sarà possibile cliccare su **"Import"**. Si aprirà una modale.
5. **Copiare il contenuto** del mio file `New_Unimol_APIs.postman_collection.json`.
6. **Tornare sulla modale di postman** e incollare il contenuto.
7. **Ora ripetere lo stesso procedimento** con il file `workspace.postman_globals.json`.
7. **Postman creerà in automatico le chiamate API all'interno dell'applicazione** che saranno subito
   testabili (ovviamente con i microservizi su docker avviati in precedenza).

---

## EXTRA

Piccola sezione extra esplicativa:

### API-Gateway

Ho inserito un piccolo api-gateway di mia sponte per capirne meglio il funzionamento. Davvero molto
basic.

### Gestione Utenti e Ruoli - Mauro

Per il mio lavoro mi sono servito del progetto finale di Mauro
disponibile [qui](https://github.com/Maurocavasinni/Gestione-Utenti-e-Ruoli). Tuttavia, è doveroso
chiarire che, a causa di carenze varie e convenzioni non rispettate, ho dovuto sensibilmente
rivedere il suo lavoro.