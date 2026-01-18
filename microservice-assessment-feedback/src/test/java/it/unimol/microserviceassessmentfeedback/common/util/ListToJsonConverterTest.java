package it.unimol.microserviceassessmentfeedback.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unimol.microserviceassessmentfeedback.dto.TeacherSurveyDto.SurveyQuestionDto;
import it.unimol.microserviceassessmentfeedback.enums.QuestionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListToJsonConverterTest {

  private ListToJsonConverter converter;

  @BeforeEach
  void setUp() {
    converter = new ListToJsonConverter();
  }

  @Test
  void testConvertToDatabaseColumn_Success() {
    SurveyQuestionDto question1 = SurveyQuestionDto.builder()
        .id("Q1")
        .questionText("Quanto è stata chiara la spiegazione?")
        .questionType(QuestionType.RATING)
        .minRating(1)
        .maxRating(5)
        .build();

    SurveyQuestionDto question2 = SurveyQuestionDto.builder()
        .id("Q2")
        .questionText("Commenti aggiuntivi")
        .questionType(QuestionType.TEXT)
        .maxLengthText(500)
        .build();

    List<SurveyQuestionDto> questions = Arrays.asList(question1, question2);

    String json = converter.convertToDatabaseColumn(questions);

    assertNotNull(json);
    assertTrue(json.contains("Q1"));
    assertTrue(json.contains("Quanto è stata chiara la spiegazione?"));
    assertTrue(json.contains("Q2"));
    assertTrue(json.contains("Commenti aggiuntivi"));
    assertTrue(json.contains("RATING"));
    assertTrue(json.contains("TEXT"));
  }

  @Test
  void testConvertToDatabaseColumn_EmptyList() {
    List<SurveyQuestionDto> emptyList = new ArrayList<>();

    String result = converter.convertToDatabaseColumn(emptyList);

    assertNull(result);
  }

  @Test
  void testConvertToDatabaseColumn_NullList() {
    String result = converter.convertToDatabaseColumn(null);

    assertNull(result);
  }

  @Test
  void testConvertToEntityAttribute_Success() {
    String json = "[{\"id\":\"Q1\",\"questionText\":\"Domanda 1\",\"questionType\":\"RATING\"," +
        "\"minRating\":1,\"maxRating\":5,\"maxLengthText\":null}," +
        "{\"id\":\"Q2\",\"questionText\":\"Domanda 2\",\"questionType\":\"TEXT\"," +
        "\"minRating\":null,\"maxRating\":null,\"maxLengthText\":255}]";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(2, result.size());

    assertEquals("Q1", result.get(0).getId());
    assertEquals("Domanda 1", result.get(0).getQuestionText());
    assertEquals(QuestionType.RATING, result.get(0).getQuestionType());
    assertEquals(1, result.get(0).getMinRating());
    assertEquals(5, result.get(0).getMaxRating());

    assertEquals("Q2", result.get(1).getId());
    assertEquals("Domanda 2", result.get(1).getQuestionText());
    assertEquals(QuestionType.TEXT, result.get(1).getQuestionType());
    assertEquals(255, result.get(1).getMaxLengthText());
  }

  @Test
  void testConvertToEntityAttribute_NullString() {
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(null);

    assertNull(result);
  }

  @Test
  void testConvertToEntityAttribute_EmptyString() {
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute("");

    assertNull(result);
  }

  @Test
  void testConvertToEntityAttribute_WhitespaceString() {
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute("   ");

    assertNull(result);
  }

  @Test
  void testConvertToEntityAttribute_InvalidJson() {
    String invalidJson = "{invalid json structure";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(invalidJson);

    assertNull(result);
  }

  @Test
  void testRoundTrip_RatingQuestion() {
    SurveyQuestionDto original = SurveyQuestionDto.builder()
        .id("Q_RATING_1")
        .questionText("Come valuti la chiarezza del docente?")
        .questionType(QuestionType.RATING)
        .minRating(1)
        .maxRating(5)
        .build();

    List<SurveyQuestionDto> originalList = Arrays.asList(original);

    // Converti in JSON
    String json = converter.convertToDatabaseColumn(originalList);
    assertNotNull(json);

    // Riconverti in oggetto
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Q_RATING_1", result.get(0).getId());
    assertEquals("Come valuti la chiarezza del docente?", result.get(0).getQuestionText());
    assertEquals(QuestionType.RATING, result.get(0).getQuestionType());
    assertEquals(1, result.get(0).getMinRating());
    assertEquals(5, result.get(0).getMaxRating());
  }

  @Test
  void testRoundTrip_TextQuestion() {
    SurveyQuestionDto original = SurveyQuestionDto.builder()
        .id("Q_TEXT_1")
        .questionText("Suggerimenti per migliorare il corso")
        .questionType(QuestionType.TEXT)
        .maxLengthText(1000)
        .build();

    List<SurveyQuestionDto> originalList = Arrays.asList(original);

    // Converti in JSON
    String json = converter.convertToDatabaseColumn(originalList);
    assertNotNull(json);

    // Riconverti in oggetto
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Q_TEXT_1", result.get(0).getId());
    assertEquals("Suggerimenti per migliorare il corso", result.get(0).getQuestionText());
    assertEquals(QuestionType.TEXT, result.get(0).getQuestionType());
    assertEquals(1000, result.get(0).getMaxLengthText());
  }

  @Test
  void testRoundTrip_MultipleQuestions() {
    SurveyQuestionDto question1 = SurveyQuestionDto.builder()
        .id("Q1")
        .questionText("Domanda rating")
        .questionType(QuestionType.RATING)
        .minRating(1)
        .maxRating(5)
        .build();

    SurveyQuestionDto question2 = SurveyQuestionDto.builder()
        .id("Q2")
        .questionText("Domanda testuale")
        .questionType(QuestionType.TEXT)
        .maxLengthText(500)
        .build();

    List<SurveyQuestionDto> original = Arrays.asList(question1, question2);

    // Converti in JSON
    String json = converter.convertToDatabaseColumn(original);
    assertNotNull(json);

    // Riconverti in oggetto
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Q1", result.get(0).getId());
    assertEquals("Q2", result.get(1).getId());
  }

  @Test
  void testConvertToDatabaseColumn_SingleItem() {
    SurveyQuestionDto question = SurveyQuestionDto.builder()
        .id("SINGLE_Q")
        .questionText("Singola domanda")
        .questionType(QuestionType.RATING)
        .build();

    List<SurveyQuestionDto> singleItemList = Arrays.asList(question);

    String json = converter.convertToDatabaseColumn(singleItemList);

    assertNotNull(json);
    assertTrue(json.contains("SINGLE_Q"));
    assertTrue(json.contains("Singola domanda"));
  }

  @Test
  void testConvertToEntityAttribute_SingleItem() {
    String json = "[{\"id\":\"Q_SINGLE\",\"questionText\":\"Una sola domanda\"," +
        "\"questionType\":\"RATING\",\"minRating\":1,\"maxRating\":5,\"maxLengthText\":null}]";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Q_SINGLE", result.get(0).getId());
    assertEquals("Una sola domanda", result.get(0).getQuestionText());
  }

  @Test
  void testConvertToDatabaseColumn_RatingQuestionWithDefaults() {
    SurveyQuestionDto question = SurveyQuestionDto.builder()
        .questionText("Domanda con defaults")
        .questionType(QuestionType.RATING)
        .build();

    List<SurveyQuestionDto> questions = Arrays.asList(question);

    String json = converter.convertToDatabaseColumn(questions);

    assertNotNull(json);
    // Verifica che il builder abbia impostato i valori di default
    assertTrue(json.contains("minRating"));
    assertTrue(json.contains("maxRating"));
  }

  @Test
  void testConvertToDatabaseColumn_TextQuestionWithDefaults() {
    SurveyQuestionDto question = SurveyQuestionDto.builder()
        .questionText("Domanda text con defaults")
        .questionType(QuestionType.TEXT)
        .build();

    List<SurveyQuestionDto> questions = Arrays.asList(question);

    String json = converter.convertToDatabaseColumn(questions);

    assertNotNull(json);
    // Verifica che il builder abbia impostato maxLengthText di default
    assertTrue(json.contains("maxLengthText"));
  }

  @Test
  void testConvertToEntityAttribute_MalformedArray() {
    String malformedJson = "[{\"id\":\"Q1\",\"questionText\":\"Question\"}"; // Manca la chiusura

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(malformedJson);

    assertNull(result);
  }

  @Test
  void testConvertToEntityAttribute_EmptyArray() {
    String emptyArrayJson = "[]";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(emptyArrayJson);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testConvertToEntityAttribute_InvalidQuestionType() {
    String json = "[{\"id\":\"Q1\",\"questionText\":\"Domanda\",\"questionType\":\"INVALID_TYPE\"}]";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    // Il parsing dovrebbe fallire a causa del tipo non valido
    assertNull(result);
  }

  @Test
  void testConvertToDatabaseColumn_NullFieldsInQuestion() {
    SurveyQuestionDto question = new SurveyQuestionDto();
    question.setId("Q_NULL");
    question.setQuestionText("Domanda con campi null");
    question.setQuestionType(QuestionType.TEXT);
    // Altri campi rimangono null

    List<SurveyQuestionDto> questions = Arrays.asList(question);

    String json = converter.convertToDatabaseColumn(questions);

    assertNotNull(json);
    assertTrue(json.contains("Q_NULL"));
    assertTrue(json.contains("null")); // Verifica presenza di campi null nel JSON
  }

  @Test
  void testConvertToEntityAttribute_MissingRequiredFields() {
    String json = "[{\"id\":\"Q1\"}]"; // Mancano campi obbligatori

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    // Jackson dovrebbe comunque parsare, impostando i campi mancanti a null
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Q1", result.get(0).getId());
    assertNull(result.get(0).getQuestionText());
  }

  @Test
  void testConvertToDatabaseColumn_LargeList() {
    List<SurveyQuestionDto> largeList = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      SurveyQuestionDto question = SurveyQuestionDto.builder()
          .id("Q_" + i)
          .questionText("Domanda numero " + i)
          .questionType(i % 2 == 0 ? QuestionType.RATING : QuestionType.TEXT)
          .build();
      largeList.add(question);
    }

    String json = converter.convertToDatabaseColumn(largeList);

    assertNotNull(json);

    // Riconverti per verificare l'integrità
    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);
    assertNotNull(result);
    assertEquals(100, result.size());
  }

  @Test
  void testConvertToEntityAttribute_SpecialCharactersInText() {
    String json = "[{\"id\":\"Q1\",\"questionText\":\"Domanda con caratteri speciali: àèéìòù €@#\"," +
        "\"questionType\":\"TEXT\",\"minRating\":null,\"maxRating\":null,\"maxLengthText\":255}]";

    List<SurveyQuestionDto> result = converter.convertToEntityAttribute(json);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getQuestionText().contains("àèéìòù"));
    assertTrue(result.get(0).getQuestionText().contains("€@#"));
  }
}