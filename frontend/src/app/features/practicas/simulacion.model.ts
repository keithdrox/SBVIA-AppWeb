export interface Simulacion {
  idSimulacion: number;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
  puntajeFinal: number;
  idEscenario?: number;
  nombreEscenario: string;
  idUsuario?: number;
  nombreUsuario?: string;
  emailUsuario?: string;
}
