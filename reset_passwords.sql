UPDATE "Usuario"
SET password_hash = '$2a$12$C3PpqduHlC/8O8fx3dnv2uBp.1UOS.tncKtbxHBB15HnlfAdMOviy';

SELECT nombre, email FROM "Usuario" ORDER BY email;
