export interface Respaldo {
  idRespaldo: number;
  nombreArchivo: string;
  tipo: string;
  estado: string;
  fechaInicio: string;
  fechaFin?: string;
  tamanioBytes?: number;
  detalles?: string;
  modalidad?: string;
  fechaProgramada?: string;
  comentario?: string;
}
