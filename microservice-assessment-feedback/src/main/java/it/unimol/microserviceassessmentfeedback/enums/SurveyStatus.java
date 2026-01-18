package it.unimol.microserviceassessmentfeedback.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum per SurveryStatus.
 */
@Schema(description = "Lo stato del questionario (e.g., DRAFT, ACTIVE, CLOSED)")
public enum SurveyStatus {
  DRAFT,
  ACTIVE,
  CLOSED
}
