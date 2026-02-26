package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to authenticate via Telegram Mini App init data")
public class TelegramAuthRequest {

  @NotBlank
  @Schema(description = "Telegram Mini App init data string", example = "query_id=AAH...")
  private String initData;
}
