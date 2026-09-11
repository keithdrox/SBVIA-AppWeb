import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RespaldoService } from './respaldo.service';
import { Respaldo } from './respaldo.model';

@Component({
  selector: 'app-gestion-respaldos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gestion-respaldos.component.html',
  styleUrls: ['./gestion-respaldos.component.css']
})
export class GestionRespaldosComponent implements OnInit, OnDestroy {
  respaldos: Respaldo[] = [];
  cargando = true;
  generando = false;
  private autoRefreshInterval: any;

  constructor(private respaldoService: RespaldoService) {}

  ngOnInit(): void {
    this.cargarRespaldos();
    // Refrescar cada 5 segundos para ver si un respaldo en progreso ya se completó
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
    const hayEnProgreso = this.respaldos.some(r => r.estado === 'EN_PROGRESO');
    if (hayEnProgreso) {
      this.respaldoService.listar().subscribe(data => this.respaldos = data);
    }
  }

  generarRespaldo(): void {
    this.generando = true;
    this.respaldoService.generar().subscribe({
      next: (nuevoRespaldo) => {
        this.respaldos.unshift(nuevoRespaldo);
        this.generando = false;
      },
      error: () => this.generando = false
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
