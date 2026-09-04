import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { Simulacion } from '../practicas/simulacion.model';
import { SimulacionService } from '../practicas/simulacion.service';

@Component({
  selector: 'app-supervision',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './supervision.component.html',
  styleUrl: './supervision.component.css'
})
export class SupervisionComponent implements OnInit {
  practicas: Simulacion[] = [];
  filtro = '';
  estado = '';
  cargando = true;
  error = '';
  esAuditor = false;

  constructor(private auth: AuthService, private simulaciones: SimulacionService) {}

  ngOnInit(): void {
    this.esAuditor = this.auth.currentUser()?.rol === 'ROLE_AUDITOR';
    this.simulaciones.getTodas().subscribe({
      next: practicas => { this.practicas = practicas; this.cargando = false; },
      error: error => { this.error = error.error?.detail ?? 'No se pudo cargar la supervisión.'; this.cargando = false; }
    });
  }

  get filtradas(): Simulacion[] {
    const texto = this.filtro.trim().toLowerCase();
    return this.practicas.filter(practica => {
      const coincideTexto = !texto || [practica.nombreUsuario, practica.emailUsuario, practica.nombreEscenario]
        .some(valor => valor?.toLowerCase().includes(texto));
      return coincideTexto && (!this.estado || practica.estado === this.estado);
    });
  }

  get finalizadas(): Simulacion[] { return this.practicas.filter(p => p.estado !== 'EN_PROGRESO'); }
  get promedio(): number {
    return this.finalizadas.length
      ? Math.round(this.finalizadas.reduce((total, p) => total + Number(p.puntajeFinal), 0) / this.finalizadas.length)
      : 0;
  }
  get aprobadas(): number { return this.practicas.filter(p => p.estado === 'APROBADA').length; }
  get conductores(): number { return new Set(this.practicas.map(p => p.idUsuario).filter(Boolean)).size; }
}
