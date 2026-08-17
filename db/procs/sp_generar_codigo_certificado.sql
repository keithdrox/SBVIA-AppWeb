CREATE SEQUENCE IF NOT EXISTS seq_certificado START 1;

CREATE OR REPLACE FUNCTION sp_generar_codigo_certificado(p_id_simulacion INTEGER, OUT codigo_certificado VARCHAR)
AS $$
BEGIN
    codigo_certificado := 'CERT-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || p_id_simulacion || '-' || nextval('seq_certificado');
END;
$$ LANGUAGE plpgsql;
