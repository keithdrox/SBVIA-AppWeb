import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EscenarioService, Escenario } from './escenario.service';
import { AuthService } from '../../auth/auth.service';
import { RouterLink } from '@angular/router';

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

  constructor(
    private escenarioService: EscenarioService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    this.isAdmin = user?.rol === 'ROLE_ADMIN';
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

  eliminar(id: number | undefined): void {
    if (id && confirm('¿Estás seguro de eliminar este escenario?')) {
      this.escenarioService.eliminar(id).subscribe({
        next: () => this.cargarEscenarios(),
        error: (err) => alert('Error eliminando: No tiene permisos')
      });
    }
  }
}
