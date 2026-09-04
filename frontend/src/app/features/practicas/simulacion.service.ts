import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Simulacion } from './simulacion.model';

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
}
