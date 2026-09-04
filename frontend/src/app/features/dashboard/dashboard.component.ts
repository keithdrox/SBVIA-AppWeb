import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { EscenarioService } from '../escenarios/escenario.service';
import { SimulacionService } from '../practicas/simulacion.service';
import { UsuarioService } from '../usuarios/usuario.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  usuario: any;
  totalEscenarios = 0;
  totalUsuarios = 0;
  totalPracticas = 0;
  promedio = 0;
  tasaAprobacion = 0;
  cargandoMetricas = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private escenarioService: EscenarioService,
    private simulacionService: SimulacionService,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.usuario = this.authService.currentUser();
    this.cargarMetricas();
  }

  private cargarMetricas(): void {
    this.escenarioService.listar(0, 1).subscribe({
      next: pagina => this.totalEscenarios = pagina.totalElements ?? pagina.content?.length ?? 0
    });
    this.simulacionService.getMisPracticas().subscribe({
      next: practicas => {
        this.totalPracticas = practicas.length;
        const finalizadas = practicas.filter(p => p.completada || p.fechaFin);
        this.promedio = finalizadas.length
          ? Math.round(finalizadas.reduce((total, p) => total + Number(p.puntajeFinal), 0) / finalizadas.length)
          : 0;
        this.tasaAprobacion = finalizadas.length
          ? Math.round(finalizadas.filter(p => Number(p.puntajeFinal) >= 70).length * 100 / finalizadas.length)
          : 0;
        this.cargandoMetricas = false;
      },
      error: () => this.cargandoMetricas = false
    });
    if (this.usuario?.rol === 'ADMINISTRADOR') {
      this.usuarioService.listar(0, 1).subscribe({
        next: pagina => this.totalUsuarios = pagina.totalElements ?? pagina.content?.length ?? 0
      });
    }
  }

  get recomendacion(): string {
    if (this.totalPracticas === 0) return 'Empieza con un escenario de dificultad baja para establecer tu primera referencia.';
    if (this.promedio < 70) return 'Repite los escenarios practicados y concéntrate en reducir las infracciones de mayor penalización.';
    if (this.promedio < 90) return 'Vas por buen camino. Prueba escenarios de mayor dificultad para fortalecer tu anticipación.';
    return 'Tu rendimiento es sobresaliente. Mantén la constancia con escenarios y condiciones variadas.';
  }

  navigate(path: string): void {
    this.router.navigate([path]);
  }

  logout(): void {
    this.authService.logout();
  }
}
