import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthApi } from '../../core/api/auth-api';
import { Notificacoes } from '../../core/erros/notificacoes';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';

/**
 * Define ou troca a senha de transação (4 a 6 dígitos), confirmando com a senha de login.
 * Também é o caminho para desbloquear a senha depois de 3 tentativas erradas.
 */
@Component({
  selector: 'app-senha-transacao',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressBarModule],
  template: `
    <h1>Senha de transação</h1>
    <p class="explicacao">
      É pedida em depósitos, saques, transferências e investimentos. Use de 4 a 6 números.
      Depois de 3 tentativas erradas ela fica bloqueada por 15 minutos — definir uma nova aqui desbloqueia.
    </p>
    @if (enviando()) {
      <mat-progress-bar mode="indeterminate" />
    }
    <form [formGroup]="formulario" (ngSubmit)="salvar()" class="formulario">
      <mat-form-field>
        <mat-label>Senha de login</mat-label>
        <input matInput type="password" formControlName="senhaLogin" autocomplete="current-password" />
        @if (formulario.controls.senhaLogin.hasError('required')) {
          <mat-error>Confirme com a sua senha de login.</mat-error>
        }
      </mat-form-field>
      <mat-form-field>
        <mat-label>Nova senha de transação</mat-label>
        <input matInput type="password" formControlName="novaSenhaTransacao" inputmode="numeric" maxlength="6" autocomplete="new-password" />
        @if (formulario.controls.novaSenhaTransacao.hasError('required')) {
          <mat-error>Informe a nova senha.</mat-error>
        } @else if (formulario.controls.novaSenhaTransacao.hasError('pattern')) {
          <mat-error>Use de 4 a 6 números.</mat-error>
        }
      </mat-form-field>
      <mat-form-field>
        <mat-label>Repita a nova senha</mat-label>
        <input matInput type="password" formControlName="confirmacao" inputmode="numeric" maxlength="6" autocomplete="new-password" />
        @if (formulario.controls.confirmacao.hasError('diferente')) {
          <mat-error>As senhas não conferem.</mat-error>
        }
      </mat-form-field>
      @if (erro()) {
        <p class="erro" role="alert">{{ erro() }}</p>
      }
      <div class="acoes">
        <button mat-flat-button type="submit" [disabled]="enviando()">Salvar</button>
      </div>
    </form>
  `,
  styles: `
    .explicacao { max-width: 560px; color: var(--mat-sys-on-surface-variant); }
    .formulario { display: flex; flex-direction: column; gap: 12px; max-width: 400px; }
    .acoes { display: flex; justify-content: flex-end; }
    .erro { color: var(--mat-sys-error); margin: 0; }
  `,
})
export class SenhaTransacao {
  private readonly api = inject(AuthApi);
  private readonly notificacoes = inject(Notificacoes);

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    senhaLogin: ['', Validators.required],
    novaSenhaTransacao: ['', [Validators.required, Validators.pattern(/^\d{4,6}$/)]],
    confirmacao: [''],
  });

  protected salvar(): void {
    const { senhaLogin, novaSenhaTransacao, confirmacao } = this.formulario.getRawValue();
    if (confirmacao !== novaSenhaTransacao) {
      this.formulario.controls.confirmacao.setErrors({ diferente: true });
    }
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.erro.set(null);
    this.api.definirSenhaTransacao(senhaLogin, novaSenhaTransacao).subscribe({
      next: () => {
        this.enviando.set(false);
        this.formulario.reset();
        this.notificacoes.sucesso('Senha de transação definida.');
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        if (!aplicarErrosDeCampo(this.formulario, erro)) {
          this.erro.set(mensagemDeErro(erro));
        }
      },
    });
  }
}
