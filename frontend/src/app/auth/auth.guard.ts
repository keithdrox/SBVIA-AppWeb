import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { map } from 'rxjs';

/**
 * Guard de autenticación asíncrono.
 *
 * Espera a que APP_INITIALIZER termine de intentar restaurar la sesión
 * (sessionReady$) antes de evaluar si el usuario está autenticado.
 * Esto evita la race condition donde el guard redirige al login
 * antes de que el refresh del token haya completado.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.waitForSessionAndCheck().pipe(
    map(isAuthenticated => {
      if (isAuthenticated) {
        return true;
      }
      // Sesión no disponible tras intentar restaurarla: redirigir al login
      return router.createUrlTree(['/login']);
    })
  );
};
