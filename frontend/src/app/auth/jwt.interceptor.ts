import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Las cookies HttpOnly solo viajan cuando Angular habilita credenciales.
  req = req.clone({ withCredentials: true });

  // No adjuntar Authorization a los endpoints que crean o renuevan credenciales.
  if (req.url.includes('/api/auth/login') || req.url.includes('/api/auth/registro') || req.url.includes('/api/auth/refresh')) {
    return next(req);
  }

  const token = authService.getAccessToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        authService.logout(false);
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
