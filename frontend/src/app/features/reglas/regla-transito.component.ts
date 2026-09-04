import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Escenario, EscenarioService } from '../escenarios/escenario.service';
import { ToastService } from '../../shared/components/toast/toast.service';
import { ReglaTransito, ReglaTransitoService } from './regla-transito.service';

@Component({
  selector: 'app-regla-transito', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './regla-transito.component.html', styleUrl: './regla-transito.component.css'
})
export class ReglaTransitoComponent implements OnInit {
  reglas: ReglaTransito[] = [];
  escenarios: Escenario[] = [];
  filtro = '';
  cargando = true;
  guardando = false;
  confirmarEliminacion?: ReglaTransito;
  formulario: ReglaTransito = this.vacio();

  constructor(private reglasService: ReglaTransitoService, private escenariosService: EscenarioService,
              private toast: ToastService) {}

  ngOnInit(): void {
    this.cargar();
    this.escenariosService.listar(0, 100).subscribe({
      next: respuesta => this.escenarios = respuesta.content,
      error: () => this.toast.showError('No se pudieron cargar los escenarios')
    });
  }

  get filtradas(): ReglaTransito[] {
    const texto = this.filtro.trim().toLowerCase();
    return this.reglas.filter(r => !texto || [r.nombre, r.categoria, r.nombreEscenario]
      .some(valor => valor?.toLowerCase().includes(texto)));
  }

  cargar(): void {
    this.reglasService.listar().subscribe({
      next: reglas => { this.reglas = reglas; this.cargando = false; },
      error: () => { this.toast.showError('No se pudieron cargar las reglas'); this.cargando = false; }
    });
  }

  editar(regla: ReglaTransito): void { this.formulario = { ...regla }; window.scrollTo({ top: 0, behavior: 'smooth' }); }
  cancelar(): void { this.formulario = this.vacio(); }

  guardar(): void {
    if (!this.formulario.nombre.trim() || !this.formulario.categoria.trim() || !this.formulario.idEscenario) return;
    this.guardando = true;
    const operacion = this.formulario.id
      ? this.reglasService.actualizar(this.formulario.id, this.formulario)
      : this.reglasService.crear(this.formulario);
    operacion.subscribe({
      next: () => { this.toast.showSuccess(this.formulario.id ? 'Regla actualizada correctamente' : 'Regla creada correctamente'); this.cancelar(); this.cargar(); this.guardando = false; },
      error: error => { this.toast.showError(error.error?.detail ?? 'No se pudo guardar la regla'); this.guardando = false; }
    });
  }

  eliminar(): void {
    const regla = this.confirmarEliminacion;
    if (!regla?.id) return;
    this.reglasService.eliminar(regla.id).subscribe({
      next: () => { this.toast.showSuccess('Regla eliminada correctamente'); this.confirmarEliminacion = undefined; this.cargar(); },
      error: error => this.toast.showError(error.error?.detail ?? 'No se pudo eliminar la regla')
    });
  }

  private vacio(): ReglaTransito { return { nombre: '', descripcion: '', categoria: '', idEscenario: null }; }
}
