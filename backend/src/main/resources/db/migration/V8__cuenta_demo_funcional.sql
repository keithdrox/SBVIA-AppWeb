-- Cuenta pública de demostración exigida para la evaluación.
-- Usuario sin privilegios administrativos; contraseña publicada en README.
UPDATE "Usuario"
   SET password_hash = '$2a$12$haOmxF2SUYtiOxDt.ySwrOasLCX1TIPhGWbD8d2brL1iQxZwWdGi2'
 WHERE email = 'conductor@sbvia.com'
   AND "id_Rol" = (SELECT "id_Rol" FROM "Rol" WHERE nombre = 'ROLE_USER');
