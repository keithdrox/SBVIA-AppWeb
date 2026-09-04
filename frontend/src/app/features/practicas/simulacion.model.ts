export interface Simulacion {
  idSimulacion: number;
  fechaInicio: string;
  fechaFin: string | null;
  puntajeFinal: number;
  completada: boolean;
  idEscenario?: number;
  nombreEscenario: string;
  idUsuario?: number;
  nombreUsuario?: string;
  correoUsuario?: string;
}
