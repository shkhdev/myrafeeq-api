--liquibase formatted sql

--changeset myrafeeq:005-seed-cities

INSERT INTO cities (id, name_en, name_ar, name_uz, name_ru, country_code, latitude, longitude, timezone,
                    recommended_method, population)
VALUES ('tashkent', 'Tashkent', 'طشقند', 'Toshkent', 'Ташкент', 'UZ', 41.2995, 69.2401, 'Asia/Tashkent', 'KARACHI',
        2571700);

--rollback DELETE FROM cities WHERE id = 'tashkent';
