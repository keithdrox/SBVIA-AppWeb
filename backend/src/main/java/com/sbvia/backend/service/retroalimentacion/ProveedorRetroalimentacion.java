package com.sbvia.backend.service.retroalimentacion;

import com.sbvia.backend.dto.RetroalimentacionIaResponse;

public interface ProveedorRetroalimentacion {

    RetroalimentacionIaResponse generar(DatosConduccion datos);

    String origen();
}
