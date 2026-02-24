package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to authenticate via Telegram Mini App init data")
public record TelegramAuthRequest(
    @NotBlank
        @Schema(description = "Telegram Mini App init data string", example = "query_id=AAH...")
        String initData) {}
