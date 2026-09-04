import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EscenarioService, Escenario } from './escenario.service';
import { AuthService } from '../../auth/auth.service';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../shared/components/toast/toast.service';

@Component({
  selector: 'app-escenario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './escenario-list.component.html',
  styleUrl: './escenario-list.component.css'
})
export class EscenarioListComponent implements OnInit {
  escenarios: Escenario[] = [];
  page = 0;
  totalPages = 0;
  isAdmin = false;
  escenarioAEliminar?: Escenario;
  eliminando = false;

  constructor(
    private escenarioService: EscenarioService,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    this.isAdmin = user?.rol === 'ADMINISTRADOR';
    this.cargarEscenarios();
  }

  cargarEscenarios(): void {
    this.escenarioService.listar(this.page).subscribe({
      next: (data) => {
        this.escenarios = data.content;
        this.totalPages = data.totalPages;
      },
      error: (err) => console.error('Error cargando escenarios', err)
    });
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 0 && nuevaPagina < this.totalPages) {
      this.page = nuevaPagina;
      this.cargarEscenarios();
    }
  }

  solicitarEliminacion(escenario: Escenario): void {
    this.escenarioAEliminar = escenario;
  }

  cancelarEliminacion(): void {
    if (!this.eliminando) this.escenarioAEliminar = undefined;
  }

  confirmarEliminacion(): void {
    const id = this.escenarioAEliminar?.id;
    if (id === undefined || this.eliminando) return;
    this.eliminando = true;
    this.escenarioService.eliminar(id).subscribe({
      next: () => {
        this.toastService.showSuccess('Escenario eliminado correctamente');
        this.escenarioAEliminar = undefined;
        this.eliminando = false;
        this.cargarEscenarios();
      },
      error: (err) => {
        console.error('Error al eliminar escenario', err);
        this.toastService.showError(err.error?.detail ?? 'No se pudo eliminar el escenario');
        this.eliminando = false;
      }
    });
  }
}
