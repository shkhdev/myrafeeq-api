package uz.myrafeeq.api.repository.projection;

import java.time.LocalDate;

public interface DateCountProjection {

  LocalDate getPrayerDate();

  Long getCount();
}
