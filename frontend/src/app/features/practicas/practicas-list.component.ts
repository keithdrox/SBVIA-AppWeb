import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SimulacionService } from './simulacion.service';
import { Simulacion } from './simulacion.model';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-practicas-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './practicas-list.component.html',
  styleUrl: './practicas-list.component.css'
})
export class PracticasListComponent implements OnInit {
  practicas: Simulacion[] = [];
  loading = true;
  error = '';

  constructor(private simulacionService: SimulacionService) {}

  ngOnInit(): void {
    this.cargarPracticas();
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
}
