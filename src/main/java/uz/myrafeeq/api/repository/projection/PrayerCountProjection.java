package uz.myrafeeq.api.repository.projection;

import uz.myrafeeq.api.enums.PrayerName;

public interface PrayerCountProjection {

  PrayerName getPrayerName();

  Long getCount();
}
