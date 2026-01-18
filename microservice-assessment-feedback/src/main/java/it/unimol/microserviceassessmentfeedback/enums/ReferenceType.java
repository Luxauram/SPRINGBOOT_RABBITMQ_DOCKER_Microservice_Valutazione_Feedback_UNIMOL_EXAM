package it.unimol.microserviceassessmentfeedback.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum per ReferenceType.
 */
@Schema(description = "Tipo di riferimento (e.g., ASSIGNMENT, EXAM)")
public enum ReferenceType {
  ASSIGNMENT,
  EXAM
}
