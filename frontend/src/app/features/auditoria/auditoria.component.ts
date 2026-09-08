import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService, BitacoraAuditoria } from './auditoria.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auditoria.component.html',
  styleUrls: ['./auditoria.component.css']
})
export class AuditoriaComponent implements OnInit {
  registros: BitacoraAuditoria[] = [];
  cargando = false;
  error = '';

  filtros = {
    tabla: '',
    operacion: '',
    usuario: '',
    fechaInicio: '',
    fechaFin: ''
  };

  modalAbierto = false;
  registroSeleccionado: BitacoraAuditoria | null = null;
  datosAntiguosObj: any = null;
  datosNuevosObj: any = null;

  constructor(private auditoriaService: AuditoriaService) {}

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.cargando = true;
    this.error = '';
    
    // Preparar fechas si están seleccionadas (añadir horas)
    const params = { ...this.filtros };
    if (params.fechaInicio) params.fechaInicio += 'T00:00:00';
    if (params.fechaFin) params.fechaFin += 'T23:59:59';

    this.auditoriaService.obtenerAuditoria(params).subscribe({
      next: (data) => {
        this.registros = data;
        this.cargando = false;
      },
      error: (err) => {
        this.error = 'No se pudo cargar el historial de auditoría.';
        this.cargando = false;
        console.error(err);
      }
    });
  }

  descargarReporte(): void {
    const params = { ...this.filtros };
    if (params.fechaInicio) params.fechaInicio += 'T00:00:00';
    if (params.fechaFin) params.fechaFin += 'T23:59:59';

    this.auditoriaService.descargarReportePdf(params).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_auditoria_${new Date().getTime()}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (err) => {
        this.error = 'Error al generar el PDF.';
        console.error(err);
      }
    });
  }

  verDetalle(registro: BitacoraAuditoria): void {
    this.registroSeleccionado = registro;
    try {
      this.datosAntiguosObj = registro.datosAnteriores ? JSON.parse(registro.datosAnteriores) : null;
    } catch { this.datosAntiguosObj = registro.datosAnteriores; }
    
    try {
      this.datosNuevosObj = registro.datosNuevos ? JSON.parse(registro.datosNuevos) : null;
    } catch { this.datosNuevosObj = registro.datosNuevos; }

    this.modalAbierto = true;
  }

  cerrarModal(): void {
    this.modalAbierto = false;
    this.registroSeleccionado = null;
  }
}
