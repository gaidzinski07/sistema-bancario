import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensagemDeErro } from './problemas';

/** Avisos curtos no rodapé da tela (snackbar). */
@Injectable({ providedIn: 'root' })
export class Notificacoes {
  private readonly snackBar = inject(MatSnackBar);

  sucesso(mensagem: string): void {
    this.snackBar.open(mensagem, 'OK', { duration: 4000, panelClass: 'aviso-sucesso' });
  }

  erro(erro: unknown): void {
    this.snackBar.open(mensagemDeErro(erro), 'Fechar', { duration: 8000, panelClass: 'aviso-erro' });
  }
}
