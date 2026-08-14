import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id?: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
  tipoLicencia?: string;
  rol: string;
  activo: boolean;
  creadoEn?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private readonly API_URL = '/api/usuarios';

  constructor(private http: HttpClient) { }

  listar(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(this.API_URL, { params });
  }

  cambiarRol(id: number, rol: string): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API_URL}/${id}/rol`, { rol });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
