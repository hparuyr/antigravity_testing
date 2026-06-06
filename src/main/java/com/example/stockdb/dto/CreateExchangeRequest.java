package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new stock exchange")
public record CreateExchangeRequest(
        @Schema(description = "Market Identifier Code", example = "XNAS")
        @NotBlank String mic,

        @Schema(description = "Exchange name", example = "NASDAQ")
        @NotBlank String name,

        @Schema(description = "Trading currency", example = "USD")
        @NotBlank String currency,

        @Schema(description = "Timezone", example = "America/New_York")
        @NotBlank String timezone
) {}
