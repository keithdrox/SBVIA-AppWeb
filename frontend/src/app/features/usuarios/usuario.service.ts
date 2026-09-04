import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id?: number;
  nombres: string;
  apellidos: string;
  nombreUsuario?: string;
  correo: string;
  telefono?: string;
  rol: string;
  cuentaBloqueada: boolean;
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
      .set('size', size.toString())
      .set('sort', 'idUsuario,desc');
    return this.http.get(this.API_URL, { params });
  }

  cambiarRol(id: number, nombreRol: string): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API_URL}/${id}/rol`, { nombreRol });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  actualizarUsuario(id: number, data: Partial<Usuario>): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API_URL}/${id}`, data);
  }
}
