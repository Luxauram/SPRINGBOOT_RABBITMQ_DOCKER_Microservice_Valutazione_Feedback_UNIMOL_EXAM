package it.unimol.microserviceassessmentfeedback.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum per QuestionType.
 */
@Schema(description = "Tipo di domanda del questionario (e.g., RATING, TEXT)")
public enum QuestionType {
  RATING,
  TEXT
}
