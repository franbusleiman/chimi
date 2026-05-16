-- =====================================================================
-- Setup mínimo de MySQL para Chimi.
-- Ejecutar como root (o un usuario con privilegios de admin).
-- Si vas a usar un schema/usuario existente, saltea CREATE DATABASE/USER
-- y otorgale los GRANT correspondientes a ese usuario.
-- =====================================================================

-- 1) Base de datos
CREATE DATABASE IF NOT EXISTS chimi
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2) Usuario para la app (% si la app se conecta desde la red Docker;
--    'localhost' si corre todo en la misma máquina vía socket)
CREATE USER IF NOT EXISTS 'chimi_app'@'%' IDENTIFIED BY 'ReemplazaEstaPassword!';

-- 3) Permisos mínimos (Flyway necesita DDL para crear/alterar tablas)
GRANT ALL PRIVILEGES ON chimi.* TO 'chimi_app'@'%';
-- Si preferís más fino (sin SUPER ni acceso a mysql.*):
--   GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP,
--         REFERENCES, CREATE VIEW, SHOW VIEW
--     ON chimi.* TO 'chimi_app'@'%';

FLUSH PRIVILEGES;

-- 4) Validación rápida
-- Desde la línea de comandos:
--   mysql -h <MYSQL_HOST> -P 3306 -u chimi_app -p chimi -e "SELECT 1;"
