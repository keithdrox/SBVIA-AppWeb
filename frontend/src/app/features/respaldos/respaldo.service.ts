import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Respaldo } from './respaldo.model';

@Injectable({
  providedIn: 'root'
})
export class RespaldoService {
  private apiUrl = '/api/respaldos';

  constructor(private http: HttpClient) {}

  listar(): Observable<Respaldo[]> {
    return this.http.get<Respaldo[]>(this.apiUrl);
  }

  generar(payload: any): Observable<Respaldo> {
    return this.http.post<Respaldo>(`${this.apiUrl}/generar`, payload);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  descargar(id: number): void {
    window.location.href = `${this.apiUrl}/descargar/${id}`;
  }
}
