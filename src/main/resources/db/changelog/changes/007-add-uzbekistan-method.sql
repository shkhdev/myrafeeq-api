--liquibase formatted sql

--changeset myrafeeq:007-add-uzbekistan-method

-- Uzbek cities use the Muslim Board of Uzbekistan method (Fajr 15.5, Isha 15.5, Hanafi, Maghrib +3)
UPDATE cities SET recommended_method = 'MBOUZ' WHERE country_code = 'UZ';
UPDATE user_preferences SET calculation_method = 'MBOUZ'
  WHERE calculation_method = 'KARACHI' AND city_id IN (SELECT id FROM cities WHERE country_code = 'UZ');

--rollback UPDATE cities SET recommended_method = 'KARACHI' WHERE country_code = 'UZ';
--rollback UPDATE user_preferences SET calculation_method = 'KARACHI' WHERE calculation_method = 'MBOUZ';
