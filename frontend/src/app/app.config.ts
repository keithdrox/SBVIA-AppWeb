import { ApplicationConfig, APP_INITIALIZER, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { routes } from './app.routes';
import { jwtInterceptor } from './auth/jwt.interceptor';
import { AuthService } from './auth/auth.service';

function initializeApp(authService: AuthService) {
  return () => authService.refreshSession();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
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
