import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-nao-encontrada',
  imports: [MatButtonModule, RouterLink],
  template: `
    <h1>Página não encontrada</h1>
    <p>O endereço não existe ou você não tem acesso a ele.</p>
    <a mat-flat-button routerLink="/inicio">Voltar ao início</a>
  `,
})
export class NaoEncontrada {}
