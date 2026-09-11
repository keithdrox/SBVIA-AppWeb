ALTER TABLE public.respaldo
    ADD COLUMN modalidad VARCHAR(50) DEFAULT 'COMPLETO' NOT NULL,
    ADD COLUMN fecha_programada TIMESTAMP,
    ADD COLUMN comentario TEXT;

COMMENT ON COLUMN public.respaldo.modalidad IS 'Indica si el respaldo es COMPLETO, SOLO_ESTRUCTURA o SOLO_DATOS';
COMMENT ON COLUMN public.respaldo.fecha_programada IS 'Fecha en la que el usuario programó la ejecución del respaldo';
COMMENT ON COLUMN public.respaldo.comentario IS 'Motivo o descripción breve del respaldo';

ALTER TABLE public.respaldo
    ADD CONSTRAINT chk_respaldo_modalidad CHECK (modalidad IN ('COMPLETO', 'SOLO_ESTRUCTURA', 'SOLO_DATOS'));
