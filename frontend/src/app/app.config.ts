import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { routes } from './app.routes';
import { jwtInterceptor } from './auth/jwt.interceptor';
import { AuthService } from './auth/auth.service';

/**
 * Factory para APP_INITIALIZER.
 * Devuelve una función que retorna el observable de refreshSession().
 * Angular espera a que el observable complete antes de montar el router
 * y ejecutar los guards — eliminando la race condition en F5.
 */
function initializeApp(authService: AuthService) {
  return () => authService.refreshSession();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([jwtInterceptor]),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AuthService],
      multi: true
    }
  ]
};
