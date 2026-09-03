-- Cambia la contraseña de UN solo usuario (el indicado por correo).
-- La contraseña mostrada es un hash BCrypt (costo 12) de ejemplo; debe
-- generarse con BCryptPasswordEncoder y sustituirse antes de ejecutar.
UPDATE "Usuario"
SET password_hash = '$2a$12$C3PpqduHlC/8O8fx3dnv2uBp.1UOS.tncKtbxHBB15HnlfAdMOviy'
WHERE email = 'conductor@sbvia.com';

SELECT nombre, email FROM "Usuario" WHERE email = 'conductor@sbvia.com' ORDER BY email;
