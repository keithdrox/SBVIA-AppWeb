import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Escenario {
  id?: number;
  nombre: string;
  descripcion: string;
  tipoVia: string;
  nivelDificultad: number;
  clima: string;
  densidadTrafico: string;
}

@Injectable({
  providedIn: 'root'
})
export class EscenarioService {
  private readonly API_URL = '/api/escenarios';

  constructor(private http: HttpClient) { }

  listar(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'idEscenario,desc');
    return this.http.get(this.API_URL, { params });
  }

  buscarPorId(id: number): Observable<Escenario> {
    return this.http.get<Escenario>(`${this.API_URL}/${id}`);
  }

  crear(escenario: Escenario): Observable<Escenario> {
    return this.http.post<Escenario>(this.API_URL, escenario);
  }

  actualizar(id: number, escenario: Escenario): Observable<Escenario> {
    return this.http.put<Escenario>(`${this.API_URL}/${id}`, escenario);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
