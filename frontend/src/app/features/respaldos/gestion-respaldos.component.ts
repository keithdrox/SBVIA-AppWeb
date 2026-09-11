import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RespaldoService } from './respaldo.service';
import { Respaldo } from './respaldo.model';

@Component({
  selector: 'app-gestion-respaldos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './gestion-respaldos.component.html',
  styleUrls: ['./gestion-respaldos.component.css']
})
export class GestionRespaldosComponent implements OnInit, OnDestroy {
  respaldos: Respaldo[] = [];
  cargando = true;
  generando = false;
  
  // Modal state
  modalAbierto = false;
  respaldoForm: FormGroup;
  
  private autoRefreshInterval: any;

  constructor(private respaldoService: RespaldoService, private fb: FormBuilder) {
    this.respaldoForm = this.fb.group({
      modalidad: ['COMPLETO', Validators.required],
      fechaProgramada: [''],
      comentario: ['']
    });
  }

  ngOnInit(): void {
    this.cargarRespaldos();
    this.autoRefreshInterval = setInterval(() => {
      this.cargarRespaldosSilencioso();
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.autoRefreshInterval) {
      clearInterval(this.autoRefreshInterval);
    }
  }

  cargarRespaldos(): void {
    this.cargando = true;
    this.respaldoService.listar().subscribe({
      next: (data) => {
        this.respaldos = data;
        this.cargando = false;
      },
      error: () => this.cargando = false
    });
  }

  cargarRespaldosSilencioso(): void {
    const hayEnProgreso = this.respaldos.some(r => r.estado === 'EN_PROGRESO' || r.estado === 'PROGRAMADO');
    if (hayEnProgreso) {
      this.respaldoService.listar().subscribe(data => this.respaldos = data);
    }
  }

  abrirModal(): void {
    this.respaldoForm.reset({ modalidad: 'COMPLETO' });
    this.modalAbierto = true;
  }

  cerrarModal(): void {
    this.modalAbierto = false;
  }

  generarRespaldo(): void {
    if (this.respaldoForm.invalid) return;
    
    this.generando = true;
    const payload = this.respaldoForm.value;
    
    this.respaldoService.generar(payload).subscribe({
      next: (nuevoRespaldo) => {
        this.respaldos.unshift(nuevoRespaldo);
        this.generando = false;
        this.cerrarModal();
      },
      error: () => {
        this.generando = false;
        this.cerrarModal();
      }
    });
  }

  descargar(id: number): void {
    this.respaldoService.descargar(id);
  }

  eliminar(id: number): void {
    if (confirm('¿Está seguro de eliminar este respaldo permanentemente?')) {
      this.respaldoService.eliminar(id).subscribe(() => {
        this.respaldos = this.respaldos.filter(r => r.idRespaldo !== id);
      });
    }
  }

  formatearBytes(bytes: number | undefined): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const dm = 2;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }
}
