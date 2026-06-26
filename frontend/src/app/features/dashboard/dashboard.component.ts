import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  usuario: any;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.usuario = this.authService.currentUser();
  }

  logout(): void {
    this.authService.logout();
  }
}
