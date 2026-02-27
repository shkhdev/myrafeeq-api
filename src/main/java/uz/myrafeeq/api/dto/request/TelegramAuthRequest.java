package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to authenticate via Telegram Mini App init data")
public class TelegramAuthRequest {

  @NotBlank @Size(max = 4096, message = "Init data too large") @Schema(description = "Telegram Mini App init data string", example = "query_id=AAH...")
  private String initData;
}
