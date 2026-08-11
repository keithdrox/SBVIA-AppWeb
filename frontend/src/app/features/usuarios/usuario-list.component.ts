import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UsuarioService, Usuario } from './usuario.service';
import { FormsModule } from '@angular/forms';

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
  rolesDisponibles = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR', 'Conductor'];

  constructor(private usuarioService: UsuarioService) { }

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
    
    if (confirm(`¿Estás seguro de cambiar el rol de ${usuario.nombre} a ${nuevoRol}?`)) {
      if (usuario.id !== undefined) {
        this.usuarioService.cambiarRol(usuario.id, nuevoRol).subscribe({
          next: () => {
            alert('Rol actualizado exitosamente');
            this.cargarUsuarios();
          },
          error: (err) => {
            console.error('Error actualizando rol', err);
            alert('No se pudo actualizar el rol');
            // reset selection visually if needed, though reloading handles it
            this.cargarUsuarios();
          }
        });
      }
    } else {
      // Revert select visual state
      this.cargarUsuarios();
    }
  }

  desactivarUsuario(id: number | undefined, nombre: string): void {
    if (id !== undefined && confirm(`¿Estás seguro de desactivar la cuenta de ${nombre}?`)) {
      this.usuarioService.eliminar(id).subscribe({
        next: () => {
          alert('Usuario desactivado');
          this.cargarUsuarios();
        },
        error: (err) => {
          console.error('Error al desactivar usuario', err);
          alert('No se pudo desactivar al usuario');
        }
      });
    }
  }
}
