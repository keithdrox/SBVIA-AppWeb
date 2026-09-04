import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { Simulacion } from '../practicas/simulacion.model';
import { SimulacionService } from '../practicas/simulacion.service';

@Component({
  selector: 'app-supervision',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './supervision.component.html',
  styleUrls: ['./supervision.component.css', './supervision-admin-actions.component.css']
})
export class SupervisionComponent implements OnInit {
  practicas: Simulacion[] = [];
  filtro = '';
  estado = '';
  cargando = true;
  error = '';
  esAuditor = false;
  esAdmin = false;

  constructor(private auth: AuthService, private simulaciones: SimulacionService) {}

  ngOnInit(): void {
    const rol = this.auth.currentUser()?.rol;
    // El catálogo actual no tiene rol AUDITOR: nadie recibe la vista de solo lectura.
    this.esAuditor = false;
    this.esAdmin = rol === 'ADMINISTRADOR';
    this.simulaciones.getTodas().subscribe({
      next: practicas => { this.practicas = practicas; this.cargando = false; },
      error: error => { this.error = error.error?.detail ?? 'No se pudo cargar la supervisión.'; this.cargando = false; }
    });
  }

  get filtradas(): Simulacion[] {
    const texto = this.filtro.trim().toLowerCase();
    return this.practicas.filter(practica => {
      const coincideTexto = !texto || [practica.nombreUsuario, practica.correoUsuario, practica.nombreEscenario]
        .some(valor => valor?.toLowerCase().includes(texto));
      return coincideTexto && this.coincideEstado(practica);
    });
  }

  private coincideEstado(practica: Simulacion): boolean {
    if (!this.estado) return true;
    const finalizada = practica.completada || !!practica.fechaFin;
    if (this.estado === 'EN_PROGRESO') return !finalizada;
    if (!finalizada) return false;
    const aprobada = Number(practica.puntajeFinal) >= 70;
    return (this.estado === 'APROBADA') === aprobada;
  }

  get finalizadas(): Simulacion[] { return this.practicas.filter(p => p.completada || !!p.fechaFin); }
  get promedio(): number {
    return this.finalizadas.length
      ? Math.round(this.finalizadas.reduce((total, p) => total + Number(p.puntajeFinal), 0) / this.finalizadas.length)
      : 0;
  }
  get aprobadas(): number {
    return this.finalizadas.filter(p => Number(p.puntajeFinal) >= 70).length;
  }
  get conductores(): number { return new Set(this.practicas.map(p => p.idUsuario).filter(Boolean)).size; }
}
