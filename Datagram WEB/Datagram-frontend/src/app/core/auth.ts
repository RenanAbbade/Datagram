import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { SessionService } from './session.service';
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  if (!req.url.startsWith(environment.apiUrl + '/')) return next(req);
  const token = session.token();
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;
  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        token &&
        session.token() === token
      )
        session.logout();
      return throwError(() => error);
    }),
  );
};
export const authGuard: CanActivateFn = () =>
  inject(SessionService).authenticated() ||
  inject(Router).createUrlTree(['/login']);
export const adminGuard: CanActivateFn = () =>
  inject(SessionService).user()?.tipoUsuario === 'Gerente' ||
  inject(Router).createUrlTree(['/feed']);
