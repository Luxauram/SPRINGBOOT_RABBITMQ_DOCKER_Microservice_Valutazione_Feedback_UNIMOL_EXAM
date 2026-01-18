package it.unimol.microserviceassessmentfeedback.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum per FeedbackCategory.
 */
@Schema(description = "I tipi di categoria del feedback (e.g., CONTENT, PRESENTATION, "
    + "CORRECTNESS, OTHER)")
public enum FeedbackCategory {
  CONTENT,
  PRESENTATION,
  CORRECTNESS,
  OTHER
}
