import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = route => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const rolesPermitidos = route.data?.['roles'] as string[] ?? [];

  return authService.waitForSessionAndCheck().pipe(
    map(autenticado => {
      if (!autenticado) return router.createUrlTree(['/login']);
      const rol = authService.currentUser()?.rol;
      return rolesPermitidos.includes(rol) ? true : router.createUrlTree(['/dashboard']);
    })
  );
};
