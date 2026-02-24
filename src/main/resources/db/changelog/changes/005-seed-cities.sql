--liquibase formatted sql

--changeset myrafeeq:005-seed-cities

INSERT INTO cities (id, name_en, name_ar, name_uz, name_ru, country_code, latitude, longitude, timezone,
                    recommended_method, population)
VALUES ('tashkent', 'Tashkent', 'طشقند', 'Toshkent', 'Ташкент', 'UZ', 41.2995, 69.2401, 'Asia/Tashkent', 'KARACHI',
        2571700),
       ('samarkand', 'Samarkand', 'سمرقند', 'Samarqand', 'Самарканд', 'UZ', 39.6542, 66.9597, 'Asia/Samarkand',
        'KARACHI', 530000),
       ('bukhara', 'Bukhara', 'بخارى', 'Buxoro', 'Бухара', 'UZ', 39.7681, 64.4556, 'Asia/Samarkand', 'KARACHI', 280000),
       ('mecca', 'Mecca', 'مكة المكرمة', 'Makka', 'Мекка', 'SA', 21.4225, 39.8262, 'Asia/Riyadh', 'UMM_AL_QURA',
        2042000),
       ('medina', 'Medina', 'المدينة المنورة', 'Madina', 'Медина', 'SA', 24.4672, 39.6112, 'Asia/Riyadh', 'UMM_AL_QURA',
        1488700),
       ('riyadh', 'Riyadh', 'الرياض', 'Riyod', 'Эр-Рияд', 'SA', 24.7136, 46.6753, 'Asia/Riyadh', 'UMM_AL_QURA',
        7676654),
       ('dubai', 'Dubai', 'دبي', 'Dubay', 'Дубай', 'AE', 25.2048, 55.2708, 'Asia/Dubai', 'DUBAI', 3478300),
       ('istanbul', 'Istanbul', 'إسطنبول', 'Istanbul', 'Стамбул', 'TR', 41.0082, 28.9784, 'Europe/Istanbul', 'TURKEY',
        15840900),
       ('cairo', 'Cairo', 'القاهرة', 'Qohira', 'Каир', 'EG', 30.0444, 31.2357, 'Africa/Cairo', 'EGYPT', 10230350),
       ('jakarta', 'Jakarta', 'جاكرتا', 'Jakarta', 'Джакарта', 'ID', -6.2088, 106.8456, 'Asia/Jakarta', 'KEMENAG',
        10562088),
       ('kuala_lumpur', 'Kuala Lumpur', 'كوالالمبور', 'Kuala Lumpur', 'Куала-Лумпур', 'MY', 3.1390, 101.6869,
        'Asia/Kuala_Lumpur', 'JAKIM', 1982100),
       ('karachi', 'Karachi', 'كراتشي', 'Karachi', 'Карачи', 'PK', 24.8607, 67.0011, 'Asia/Karachi', 'KARACHI',
        16093786),
       ('lahore', 'Lahore', 'لاهور', 'Lahor', 'Лахор', 'PK', 31.5204, 74.3587, 'Asia/Karachi', 'KARACHI', 13095166),
       ('dhaka', 'Dhaka', 'دكا', 'Dakka', 'Дакка', 'BD', 23.8103, 90.4125, 'Asia/Dhaka', 'KARACHI', 22478116),
       ('moscow', 'Moscow', 'موسكو', 'Moskva', 'Москва', 'RU', 55.7558, 37.6173, 'Europe/Moscow', 'MWL', 12655050),
       ('london', 'London', 'لندن', 'London', 'Лондон', 'GB', 51.5074, -0.1278, 'Europe/London', 'MWL', 9002488),
       ('new_york', 'New York', 'نيويورك', 'Nyu-York', 'Нью-Йорк', 'US', 40.7128, -74.0060, 'America/New_York', 'ISNA',
        8336817),
       ('doha', 'Doha', 'الدوحة', 'Doha', 'Доха', 'QA', 25.2854, 51.5310, 'Asia/Qatar', 'QATAR', 956457),
       ('kuwait_city', 'Kuwait City', 'مدينة الكويت', 'Quvayt shahri', 'Эль-Кувейт', 'KW', 29.3759, 47.9774,
        'Asia/Kuwait', 'KUWAIT', 2989000),
       ('singapore', 'Singapore', 'سنغافورة', 'Singapur', 'Сингапур', 'SG', 1.3521, 103.8198, 'Asia/Singapore',
        'SINGAPORE', 5685800);

--rollback DELETE FROM cities WHERE id IN ('tashkent','samarkand','bukhara','mecca','medina','riyadh','dubai','istanbul','cairo','jakarta','kuala_lumpur','karachi','lahore','dhaka','moscow','london','new_york','doha','kuwait_city','singapore');
