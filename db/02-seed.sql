-- Sample data so the app has something to show.
-- Demo account:  demo@ecomm.test / Demo1234

INSERT INTO users (email, password_hash, first_name, last_name, phone) VALUES
    ('demo@ecomm.test', 'Wh9GXHuyNcBVkmqLDNKoyA==:GeMjwXKoCJRcNgIBD5Ou1ullCxY+snJ7ASfIvut6/VM=', 'Demo', 'Seller', '+40 700 000 000')
ON CONFLICT (email) DO NOTHING;

INSERT INTO car_makes (make_name) VALUES
    ('Audi'), ('BMW'), ('Dacia'), ('Ford'), ('Honda'), ('Mercedes-Benz'),
    ('Skoda'), ('Tesla'), ('Toyota'), ('Volkswagen')
ON CONFLICT (make_name) DO NOTHING;

INSERT INTO car_models (make_id, model_name, body_type)
SELECT mk.make_id, v.model_name, v.body_type
FROM (VALUES
    ('Audi', 'A4', 'Sedan'), ('Audi', 'Q5', 'SUV'), ('Audi', 'A3', 'Hatchback'),
    ('BMW', '320d', 'Sedan'), ('BMW', 'X5', 'SUV'), ('BMW', 'Z4', 'Convertible'),
    ('Dacia', 'Duster', 'SUV'), ('Dacia', 'Logan', 'Sedan'), ('Dacia', 'Sandero', 'Hatchback'),
    ('Ford', 'Focus', 'Hatchback'), ('Ford', 'Mustang', 'Coupe'), ('Ford', 'Ranger', 'Pickup'),
    ('Honda', 'Civic', 'Hatchback'), ('Honda', 'CR-V', 'SUV'),
    ('Mercedes-Benz', 'C-Class', 'Sedan'), ('Mercedes-Benz', 'Vito', 'Minivan'),
    ('Skoda', 'Octavia', 'Wagon'), ('Skoda', 'Kodiaq', 'SUV'),
    ('Tesla', 'Model 3', 'Electric'), ('Tesla', 'Model Y', 'Electric'),
    ('Toyota', 'Corolla', 'Sedan'), ('Toyota', 'RAV4', 'SUV'), ('Toyota', 'Yaris', 'Hatchback'),
    ('Volkswagen', 'Golf', 'Hatchback'), ('Volkswagen', 'Passat', 'Wagon'), ('Volkswagen', 'Touran', 'Minivan')
) AS v (make_name, model_name, body_type)
JOIN car_makes mk ON mk.make_name = v.make_name
ON CONFLICT (make_id, model_name) DO NOTHING;

INSERT INTO car_listings (user_id, model_id, title, description, price, year, mileage, fuel_type, transmission, color, location)
SELECT u.user_id, m.model_id, v.title, v.description, v.price, v.year, v.mileage, v.fuel, v.trans, v.color, v.location
FROM (VALUES
    ('Volkswagen', 'Golf', 'VW Golf 7 1.6 TDI - one owner, full service history', 'Well kept family hatchback, service book stamped at every interval. New tyres this spring.', 11900, 2016, 148000, 'Diesel', 'Manual', 'Silver', 'Cluj-Napoca'),
    ('Audi', 'A4', 'Audi A4 2.0 TDI S-line, xenon, leather seats', 'Comfortable and economical. S-line package, heated seats, parking sensors.', 17800, 2017, 165000, 'Diesel', 'Automatic', 'Black', 'Bucharest'),
    ('Tesla', 'Model 3', 'Tesla Model 3 Long Range, autopilot included', 'Battery health 94%. Includes home charger and winter tyres.', 31500, 2021, 62000, 'Electric', 'Automatic', 'White', 'Timisoara'),
    ('Dacia', 'Duster', 'Dacia Duster 1.5 dCi 4x4 - great for the mountains', 'Reliable 4x4, ideal for winter roads. Tow bar fitted.', 12400, 2019, 91000, 'Diesel', 'Manual', 'Orange', 'Brasov'),
    ('BMW', 'X5', 'BMW X5 xDrive30d, panoramic roof, M package', 'Powerful and comfortable SUV, air suspension, full options.', 38900, 2018, 120000, 'Diesel', 'Automatic', 'Grey', 'Cluj-Napoca'),
    ('Toyota', 'Corolla', 'Toyota Corolla 1.8 Hybrid - low consumption', 'Hybrid with 5 litres per 100 km in the city. Warranty until 2027.', 21500, 2022, 34000, 'Hybrid', 'Automatic', 'Blue', 'Iasi'),
    ('Skoda', 'Octavia', 'Skoda Octavia Combi 2.0 TDI, perfect for families', 'Huge boot, tow bar, new brakes and timing belt done.', 14200, 2017, 172000, 'Diesel', 'Manual', 'Grey', 'Oradea'),
    ('Ford', 'Mustang', 'Ford Mustang 5.0 GT - V8 sound, garage kept', 'Weekend car, never used in winter. Sports exhaust, original paint.', 42000, 2019, 38000, 'Gasoline', 'Manual', 'Red', 'Bucharest'),
    ('Honda', 'Civic', 'Honda Civic 1.0 VTEC Turbo - sporty and efficient', 'Lively turbo engine, adaptive cruise, Apple CarPlay.', 18600, 2020, 52000, 'Gasoline', 'Manual', 'White', 'Sibiu'),
    ('Mercedes-Benz', 'C-Class', 'Mercedes C 220 d AMG Line - perfect condition', 'AMG line interior and exterior, Burmester sound system.', 27900, 2019, 98000, 'Diesel', 'Automatic', 'Silver', 'Cluj-Napoca'),
    ('Ford', 'Ranger', 'Ford Ranger Wildtrak 3.2 - double cab pickup', 'Hardtop, roll-bar, off-road tyres. Used for work, always maintained.', 29900, 2018, 134000, 'Diesel', 'Automatic', 'Black', 'Constanta'),
    ('Volkswagen', 'Passat', 'VW Passat Variant 2.0 TDI DSG, adaptive cruise', 'Business class comfort, large boot, no accidents.', 16400, 2017, 189000, 'Diesel', 'Automatic', 'Blue', 'Timisoara')
) AS v (make_name, model_name, title, description, price, year, mileage, fuel, trans, color, location)
JOIN car_makes mk ON mk.make_name = v.make_name
JOIN car_models m ON m.make_id = mk.make_id AND m.model_name = v.model_name
CROSS JOIN (SELECT user_id FROM users WHERE email = 'demo@ecomm.test') u
WHERE NOT EXISTS (SELECT 1 FROM car_listings WHERE title = v.title);
