import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Simulacion } from './simulacion.model';
import { SimulacionService } from './simulacion.service';

interface Infraccion {
  nombre: string;
  penalizacion: number;
  cantidad: number;
}

@Component({
  selector: 'app-simulacion',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './simulacion.component.html',
  styleUrl: './simulacion.component.css'
})
export class SimulacionComponent implements OnInit, OnDestroy {
  practica?: Simulacion;
  resultado?: Simulacion;
  segundos = 0;
  cargando = true;
  finalizando = false;
  error = '';
  private temporizador?: ReturnType<typeof setInterval>;

  infracciones: Infraccion[] = [
    { nombre: 'Exceso de velocidad', penalizacion: 15, cantidad: 0 },
    { nombre: 'No respetar una señal', penalizacion: 20, cantidad: 0 },
    { nombre: 'Cambio de carril inseguro', penalizacion: 10, cantidad: 0 },
    { nombre: 'Frenado brusco', penalizacion: 5, cantidad: 0 }
  ];

  constructor(private route: ActivatedRoute, private simulacionService: SimulacionService) {}

  ngOnInit(): void {
    const idEscenario = Number(this.route.snapshot.paramMap.get('idEscenario'));
    if (!Number.isInteger(idEscenario) || idEscenario <= 0) {
      this.error = 'El escenario seleccionado no es válido.';
      this.cargando = false;
      return;
    }
    this.simulacionService.iniciar(idEscenario).subscribe({
      next: practica => {
        this.practica = practica;
        this.cargando = false;
        this.temporizador = setInterval(() => this.segundos++, 1000);
      },
      error: err => {
        this.error = err.error?.detail ?? 'No se pudo iniciar la práctica.';
        this.cargando = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.temporizador) clearInterval(this.temporizador);
  }

  registrar(infraccion: Infraccion): void {
    infraccion.cantidad++;
  }

  get puntaje(): number {
    const descuento = this.infracciones.reduce(
      (total, infraccion) => total + infraccion.penalizacion * infraccion.cantidad, 0);
    return Math.max(0, 100 - descuento);
  }

  get tiempo(): string {
    const minutos = Math.floor(this.segundos / 60).toString().padStart(2, '0');
    const segundos = (this.segundos % 60).toString().padStart(2, '0');
    return `${minutos}:${segundos}`;
  }

  finalizar(): void {
    if (!this.practica || this.finalizando) return;
    this.finalizando = true;
    this.simulacionService.finalizar(this.practica.idSimulacion, this.puntaje).subscribe({
      next: resultado => {
        this.resultado = resultado;
        this.finalizando = false;
        if (this.temporizador) clearInterval(this.temporizador);
      },
      error: err => {
        this.error = err.error?.detail ?? 'No se pudo finalizar la práctica.';
        this.finalizando = false;
      }
    });
  }
}
