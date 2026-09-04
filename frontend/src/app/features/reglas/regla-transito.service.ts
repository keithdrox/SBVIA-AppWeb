import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ReglaTransito {
  id?: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  categoria: string;
  penalizacionBase: number;
  activa?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ReglaTransitoService {
  private readonly url = '/api/reglas-transito';
  constructor(private http: HttpClient) {}
  listar(): Observable<ReglaTransito[]> { return this.http.get<ReglaTransito[]>(this.url); }
  crear(regla: ReglaTransito): Observable<ReglaTransito> { return this.http.post<ReglaTransito>(this.url, regla); }
  actualizar(id: number, regla: ReglaTransito): Observable<ReglaTransito> { return this.http.put<ReglaTransito>(`${this.url}/${id}`, regla); }
  eliminar(id: number): Observable<void> { return this.http.delete<void>(`${this.url}/${id}`); }
}
