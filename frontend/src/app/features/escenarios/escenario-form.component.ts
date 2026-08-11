import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EscenarioService, Escenario } from './escenario.service';

@Component({
  selector: 'app-escenario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './escenario-form.component.html',
  styleUrl: './escenario-form.component.css'
})
export class EscenarioFormComponent implements OnInit {
  escenarioForm: FormGroup;
  isEditMode = false;
  escenarioId: number | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private escenarioService: EscenarioService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.escenarioForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      descripcion: ['', [Validators.maxLength(500)]],
      tipoVia: ['', Validators.required],
      nivelDificultad: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
      clima: ['', Validators.required],
      densidadTrafico: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.escenarioId = +idParam;
      this.loadEscenarioData(this.escenarioId);
    }
  }

  loadEscenarioData(id: number): void {
    this.loading = true;
    this.escenarioService.buscarPorId(id).subscribe({
      next: (escenario) => {
        this.escenarioForm.patchValue({
          nombre: escenario.nombre,
          descripcion: escenario.descripcion,
          tipoVia: escenario.tipoVia,
          nivelDificultad: escenario.nivelDificultad,
          clima: escenario.clima,
          densidadTrafico: escenario.densidadTrafico
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar los datos del escenario.';
        console.error(err);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.escenarioForm.invalid) {
      this.escenarioForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    const escenarioData: Escenario = this.escenarioForm.value;

    if (this.isEditMode && this.escenarioId) {
      this.escenarioService.actualizar(this.escenarioId, escenarioData).subscribe({
        next: () => {
          this.router.navigate(['/escenarios']);
        },
        error: (err) => {
          this.errorMessage = 'Error al actualizar el escenario.';
          console.error(err);
          this.loading = false;
        }
      });
    } else {
      this.escenarioService.crear(escenarioData).subscribe({
        next: () => {
          this.router.navigate(['/escenarios']);
        },
        error: (err) => {
          this.errorMessage = 'Error al crear el escenario.';
          console.error(err);
          this.loading = false;
        }
      });
    }
  }
}
