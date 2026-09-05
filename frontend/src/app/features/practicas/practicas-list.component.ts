import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InformeIA, SimulacionService } from './simulacion.service';
import { Simulacion } from './simulacion.model';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Escenario, EscenarioService } from '../escenarios/escenario.service';

@Component({
  selector: 'app-practicas-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './practicas-list.component.html',
  styleUrl: './practicas-list.component.css'
})
export class PracticasListComponent implements OnInit {
  practicas: Simulacion[] = [];
  loading = true;
  error = '';
  escenarios: Escenario[] = [];
  idEscenarioSeleccionado: number | null = null;
  cargandoEscenarios = true;
  filtro: 'todas' | 'finalizadas' | 'pendientes' = 'todas';
  practicaSeleccionada: Simulacion | null = null;
  informeSeleccionado: InformeIA | null = null;
  cargandoInforme = false;
  errorInforme = '';

  constructor(private simulacionService: SimulacionService, private escenarioService: EscenarioService) {}

  ngOnInit(): void {
    this.cargarPracticas();
    this.escenarioService.listar(0, 100).subscribe({
      next: respuesta => {
        this.escenarios = respuesta.content;
        this.idEscenarioSeleccionado = this.escenarios[0]?.id ?? null;
        this.cargandoEscenarios = false;
      },
      error: () => this.cargandoEscenarios = false
    });
  }

  cargarPracticas(): void {
    this.simulacionService.getMisPracticas().subscribe({
      next: (data) => {
        this.practicas = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'No se pudieron cargar las prácticas.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  get finalizadas(): Simulacion[] {
    return this.practicas.filter(practica => practica.completada || !!practica.fechaFin);
  }

  get promedio(): number {
    if (this.finalizadas.length === 0) return 0;
    const total = this.finalizadas.reduce((suma, practica) => suma + Number(practica.puntajeFinal), 0);
    return Math.round((total / this.finalizadas.length) * 10) / 10;
  }

  get aprobadas(): number {
    return this.finalizadas.filter(practica => Number(practica.puntajeFinal) >= 70).length;
  }

  get escenarioSeleccionado(): Escenario | undefined {
    return this.escenarios.find(escenario => escenario.id === this.idEscenarioSeleccionado);
  }

  get practicasFiltradas(): Simulacion[] {
    if (this.filtro === 'finalizadas') return this.finalizadas;
    if (this.filtro === 'pendientes') return this.practicas.filter(practica => !practica.completada && !practica.fechaFin);
    return this.practicas;
  }

  get pendientes(): number {
    return this.practicas.length - this.finalizadas.length;
  }

  verInforme(practica: Simulacion): void {
    if (!practica.completada && !practica.fechaFin) return;
    if (this.practicaSeleccionada?.idSimulacion === practica.idSimulacion) {
      this.cerrarInforme();
      return;
    }
    this.practicaSeleccionada = practica;
    this.informeSeleccionado = null;
    this.errorInforme = '';
    this.cargandoInforme = true;
    this.simulacionService.getRetroalimentacion(practica.idSimulacion).subscribe({
      next: informe => {
        this.informeSeleccionado = informe;
        this.cargandoInforme = false;
      },
      error: () => {
        this.errorInforme = 'No se pudo recuperar el informe de esta práctica.';
        this.cargandoInforme = false;
      }
    });
  }

  cerrarInforme(): void {
    this.practicaSeleccionada = null;
    this.informeSeleccionado = null;
    this.errorInforme = '';
    this.cargandoInforme = false;
  }
}
