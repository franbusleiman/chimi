-- =============================================================
-- Chimi V2 — seed data inicial para el tenant "demo" (MySQL)
-- =============================================================

-- TENANT
INSERT INTO tenants (slug, name, timezone, parallel_slots, min_lead_minutes,
                     max_lead_days, slot_granularity_minutes,
                     allow_cancel_by_wpp, allow_reschedule_by_wpp, active)
VALUES ('demo', 'Veterinaria Demo', 'America/Argentina/Buenos_Aires',
        1, 60, 60, 15, 1, 1, 1);

SET @tid = LAST_INSERT_ID();

-- BRANDING
INSERT INTO branding (tenant_id, display_name, primary_color, secondary_color, greeting, tone)
VALUES (@tid, 'Veterinaria Demo', '#2C7A7B', '#F6AD55',
        '¡Hola! Soy el asistente virtual de Veterinaria Demo. ¿En qué te puedo ayudar?',
        'informal');

-- WORKING HOURS (L-V 9-13 y 16-20, sábado 9-13)
INSERT INTO working_hours (tenant_id, day_of_week, start_time, end_time) VALUES
 (@tid, 'MONDAY',    '09:00:00', '13:00:00'),
 (@tid, 'MONDAY',    '16:00:00', '20:00:00'),
 (@tid, 'TUESDAY',   '09:00:00', '13:00:00'),
 (@tid, 'TUESDAY',   '16:00:00', '20:00:00'),
 (@tid, 'WEDNESDAY', '09:00:00', '13:00:00'),
 (@tid, 'WEDNESDAY', '16:00:00', '20:00:00'),
 (@tid, 'THURSDAY',  '09:00:00', '13:00:00'),
 (@tid, 'THURSDAY',  '16:00:00', '20:00:00'),
 (@tid, 'FRIDAY',    '09:00:00', '13:00:00'),
 (@tid, 'FRIDAY',    '16:00:00', '20:00:00'),
 (@tid, 'SATURDAY',  '09:00:00', '13:00:00');

-- APPOINTMENT TYPES (los 10 que pidió la vete, con duraciones tentativas)
INSERT INTO appointment_types (tenant_id, code, name, duration_minutes, display_order, active) VALUES
 (@tid, 'PRIMERA_VEZ',    'Consulta primera vez',       45, 1, 1),
 (@tid, 'DERIVADO',       'Consulta de derivados',      45, 2, 1),
 (@tid, 'GENERAL',        'Consulta general',           30, 3, 1),
 (@tid, 'ESPECIALISTA',   'Consulta con especialista',  45, 4, 1),
 (@tid, 'CONTROL',        'Controles generales',        20, 5, 1),
 (@tid, 'POSTQUIRURGICO', 'Controles post quirúrgico',  30, 6, 1),
 (@tid, 'VACUNA',         'Vacunas o inyectables',      15, 7, 1),
 (@tid, 'RADIOGRAFIA',    'Radiografías',               30, 8, 1),
 (@tid, 'LABORATORIO',    'Laboratorio',                15, 9, 1),
 (@tid, 'OTROS',          'Otros',                      30, 10, 1);

-- USUARIO ADMIN INICIAL: admin@demo.chimi / changeme
-- (regenerá este hash con tu propio bcrypt si querés otra password)
INSERT INTO app_users (tenant_id, email, password_hash, full_name, role, active)
VALUES (@tid, 'admin@demo.chimi',
        '$2a$10$3qhPK8mWqkE1xz9JfYAhdeJp3v3IfNXjGCl6mHukn2DDVT3iJoO1G',
        'Admin Demo', 'OWNER', 1);

-- FAQs demo
INSERT INTO faqs (tenant_id, category, question, answer, display_order, active) VALUES
 (@tid, 'CLINIC',   '¿Cuáles son los horarios de atención?',
                    'Atendemos de lunes a viernes de 9 a 13 y de 16 a 20, sábados de 9 a 13.', 1, 1),
 (@tid, 'CLINIC',   '¿Hacen urgencias?',
                    'Sí, las urgencias se atienden en el horario de la clínica. Fuera de horario contactanos por este mismo chat y te derivamos.', 2, 1),
 (@tid, 'CLINIC',   '¿Dónde están ubicados?',
                    'Estamos en la dirección XXX (completar). ¡Te esperamos!', 3, 1),
 (@tid, 'PRODUCTS', '¿Venden alimentos balanceados?',
                    'Sí, manejamos las principales marcas. Pasá por el local o consultá stock por este chat.', 1, 1),
 (@tid, 'PRODUCTS', '¿Tienen accesorios?',
                    'Tenemos collares, correas, comederos, juguetes y más. Consultanos por el producto que buscás.', 2, 1);
