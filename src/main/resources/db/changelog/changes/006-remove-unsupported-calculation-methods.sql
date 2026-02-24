--liquibase formatted sql

--changeset myrafeeq:006-remove-unsupported-calculation-methods

-- TURKEY (Fajr 18, Isha 17) has identical angles to MWL
UPDATE cities
SET recommended_method = 'MWL'
WHERE recommended_method = 'TURKEY';

-- JAKIM and KEMENAG (Fajr 20, Isha 18) have identical angles to SINGAPORE
UPDATE cities
SET recommended_method = 'SINGAPORE'
WHERE recommended_method IN ('JAKIM', 'KEMENAG');

-- Also update any user preferences that reference removed methods
UPDATE user_preferences
SET calculation_method = 'MWL'
WHERE calculation_method = 'TURKEY';
UPDATE user_preferences
SET calculation_method = 'MWL'
WHERE calculation_method = 'TEHRAN';
UPDATE user_preferences
SET calculation_method = 'SINGAPORE'
WHERE calculation_method IN ('JAKIM', 'KEMENAG');

--rollback UPDATE cities SET recommended_method = 'TURKEY' WHERE id = 'istanbul';
--rollback UPDATE cities SET recommended_method = 'KEMENAG' WHERE id = 'jakarta';
--rollback UPDATE cities SET recommended_method = 'JAKIM' WHERE id = 'kuala_lumpur';
--rollback UPDATE user_preferences SET calculation_method = 'TURKEY' WHERE calculation_method = 'MWL';
--rollback UPDATE user_preferences SET calculation_method = 'TEHRAN' WHERE calculation_method = 'MWL';
--rollback UPDATE user_preferences SET calculation_method = 'JAKIM' WHERE calculation_method = 'SINGAPORE';
--rollback UPDATE user_preferences SET calculation_method = 'KEMENAG' WHERE calculation_method = 'SINGAPORE';
