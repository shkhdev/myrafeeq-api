package uz.myrafeeq.api.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeFormat {
  TWELVE_HOUR("12h"),
  TWENTY_FOUR_HOUR("24h");

  @JsonValue private final String value;
}
