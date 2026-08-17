import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UsuarioService, Usuario } from './usuario.service';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../shared/components/toast/toast.service';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './usuario-list.component.html',
  styleUrl: './usuario-list.component.css'
})
export class UsuarioListComponent implements OnInit {
  usuarios: Usuario[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  rolesDisponibles = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR', 'ROLE_AUDITOR'];

  // Modal de edición
  mostrarModal = false;
  usuarioEditando: Partial<Usuario> = {};
  guardando = false;

  // Modal de Confirmación
  mostrarConfirmacion = false;
  mensajeConfirmacion = '';
  accionConfirmacion: () => void = () => {};

  constructor(
    private usuarioService: UsuarioService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.usuarioService.listar(this.page, this.size).subscribe({
      next: (data) => {
        this.usuarios = data.content;
        this.totalPages = data.totalPages;
      },
      error: (err) => {
        console.error('Error al cargar usuarios', err);
      }
    });
  }

  cambiarPagina(newPage: number): void {
    this.page = newPage;
    this.cargarUsuarios();
  }

  cambiarRol(usuario: Usuario, nuevoRol: string): void {
    if (usuario.rol === nuevoRol) return;
    
    this.abrirConfirmacion(
      `¿Estás seguro de cambiar el rol de ${usuario.nombre} a ${nuevoRol}?`,
      () => {
        if (usuario.id !== undefined) {
          this.usuarioService.cambiarRol(usuario.id, nuevoRol).subscribe({
            next: () => {
              this.toastService.showSuccess('Rol actualizado exitosamente');
              this.cargarUsuarios();
            },
            error: (err) => {
              console.error('Error actualizando rol', err);
              const errMsg = err.error?.detail || err.error?.message || 'No se pudo actualizar el rol';
              this.toastService.showError(errMsg);
              this.cargarUsuarios();
            }
          });
        }
      },
      () => {
        this.cargarUsuarios(); // revert select visual state if cancelled
      }
    );
  }

  desactivarUsuario(id: number | undefined, nombre: string): void {
    if (id !== undefined) {
      this.abrirConfirmacion(
        `¿Estás seguro de desactivar la cuenta de ${nombre}?`,
        () => {
          this.usuarioService.eliminar(id).subscribe({
            next: () => {
              this.toastService.showSuccess('Usuario desactivado exitosamente');
              this.cargarUsuarios();
            },
            error: (err) => {
              console.error('Error al desactivar usuario', err);
              const errMsg = err.error?.detail || err.error?.message || 'No se pudo desactivar al usuario';
              this.toastService.showError(errMsg);
            }
          });
        }
      );
    }
  }

  // Lógica del Modal
  abrirModalEditar(usuario: Usuario): void {
    this.usuarioEditando = { ...usuario };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.usuarioEditando = {};
  }

  guardarCambiosUsuario(): void {
    if (!this.usuarioEditando.id) return;
    
    this.guardando = true;
    this.usuarioService.actualizarUsuario(this.usuarioEditando.id, this.usuarioEditando).subscribe({
      next: () => {
        this.toastService.showSuccess('Usuario actualizado exitosamente');
        this.guardando = false;
        this.cerrarModal();
        this.cargarUsuarios();
      },
      error: (err) => {
        console.error('Error al actualizar usuario', err);
        const errMsg = err.error?.detail || err.error?.message || 'Error desconocido al actualizar';
        this.toastService.showError(errMsg);
        this.guardando = false;
      }
    });
  }

  // Lógica de Confirmación
  abrirConfirmacion(mensaje: string, accion: () => void, accionCancelar: () => void = () => {}): void {
    this.mensajeConfirmacion = mensaje;
    this.accionConfirmacion = () => {
      accion();
      this.cerrarConfirmacion();
    };
    this.mostrarConfirmacion = true;
    
    // Si queremos ejecutar algo al cancelar, lo guardamos o lo ejecutamos directo.
    // Por simplicidad, ejecutaremos accionCancelar() si el usuario cierra el modal.
    this.cancelarCallback = accionCancelar;
  }

  cancelarCallback: () => void = () => {};

  cerrarConfirmacion(): void {
    this.mostrarConfirmacion = false;
    this.mensajeConfirmacion = '';
    this.accionConfirmacion = () => {};
    this.cancelarCallback();
    this.cancelarCallback = () => {};
  }
}
