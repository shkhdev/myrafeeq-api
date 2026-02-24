package uz.myrafeeq.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatsPeriod {
  WEEK(7),
  MONTH(30),
  YEAR(365);

  private final int days;
}
