import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Simulacion } from './simulacion.model';

export interface MetricasConduccion {
  duracionSegundos: number;
  velocidadPromedio: number;
  velocidadMaxima: number;
  excesosVelocidad: number;
  colisiones: number;
  salidasCarril: number;
  semaforosIgnorados: number;
  distanciaInsegura: number;
  semaforosRespetados: number;
}

export interface InformeIA {
  resumen: string;
  aciertos: string[];
  errores: string[];
  nivelRiesgo: string;
  recomendaciones: string[];
  puntaje: number;
  mensajeMotivador: string;
  comparacion: string | null;
  origen: string;
}

export interface ResultadoConduccion {
  simulacion: Simulacion;
  retroalimentacion: InformeIA;
}

@Injectable({
  providedIn: 'root'
})
export class SimulacionService {
  private apiUrl = '/api/simulaciones';

  constructor(private http: HttpClient) {}

  getMisPracticas(): Observable<Simulacion[]> {
    return this.http.get<Simulacion[]>(`${this.apiUrl}/mis-practicas`);
  }

  getTodas(): Observable<Simulacion[]> {
    return this.http.get<Simulacion[]>(this.apiUrl);
  }

  iniciar(idEscenario: number): Observable<Simulacion> {
    return this.http.post<Simulacion>(`${this.apiUrl}/iniciar/${idEscenario}`, {});
  }

  finalizar(idSimulacion: number, puntajeFinal: number): Observable<Simulacion> {
    return this.http.post<Simulacion>(`${this.apiUrl}/${idSimulacion}/finalizar`, { puntajeFinal });
  }

  finalizarConduccion(idSimulacion: number, metricas: MetricasConduccion): Observable<ResultadoConduccion> {
    return this.http.post<ResultadoConduccion>(`${this.apiUrl}/${idSimulacion}/conduccion/finalizar`, metricas);
  }

  getRetroalimentacion(idSimulacion: number): Observable<InformeIA> {
    return this.http.get<InformeIA>(`${this.apiUrl}/${idSimulacion}/retroalimentacion`);
  }
}
