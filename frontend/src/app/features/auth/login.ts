import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { Sessao } from '../../core/auth/sessao';
import { mensagemDeErro } from '../../core/erros/problemas';
import { validadorCpf } from '../../shared/cpf';

/** Login único para clientes e administradores (no legado eram duas telas e dois servlets). */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <div class="fundo">
      <mat-card class="cartao" appearance="outlined">
        @if (enviando()) {
          <mat-progress-bar mode="indeterminate" />
        }
        <mat-card-header>
          <mat-card-title>Banco UFF</mat-card-title>
          <mat-card-subtitle>Entre com seu CPF e senha</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (sessao() === 'expirada') {
            <p class="aviso" role="status">Sua sessão expirou. Entre novamente.</p>
          }
          @if (erro()) {
            <p class="erro" role="alert">{{ erro() }}</p>
          }
          <form [formGroup]="formulario" (ngSubmit)="entrar()" class="formulario">
            <mat-form-field>
              <mat-label>CPF</mat-label>
              <input matInput formControlName="cpf" inputmode="numeric" autocomplete="username" placeholder="000.000.000-00" />
              @if (formulario.controls.cpf.hasError('required')) {
                <mat-error>Informe o CPF.</mat-error>
              } @else if (formulario.controls.cpf.hasError('cpf')) {
                <mat-error>CPF inválido.</mat-error>
              }
            </mat-form-field>
            <mat-form-field>
              <mat-label>Senha</mat-label>
              <input matInput formControlName="senha" [type]="mostrarSenha() ? 'text' : 'password'" autocomplete="current-password" />
              <button mat-icon-button matSuffix type="button" (click)="mostrarSenha.set(!mostrarSenha())"
                      [attr.aria-label]="mostrarSenha() ? 'Ocultar senha' : 'Mostrar senha'">
                <mat-icon>{{ mostrarSenha() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (formulario.controls.senha.hasError('required')) {
                <mat-error>Informe a senha.</mat-error>
              }
            </mat-form-field>
            <button mat-flat-button type="submit" [disabled]="enviando()">Entrar</button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: `
    .fundo { min-height: 100%; display: grid; place-items: center; padding: 16px; box-sizing: border-box;
             background: var(--mat-sys-surface-container); }
    .cartao { width: 100%; max-width: 400px; }
    .formulario { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
    .erro { color: var(--mat-sys-error); margin: 12px 0 0; }
    .aviso { color: var(--mat-sys-on-surface-variant); margin: 12px 0 0; }
  `,
})
export class Login {
  private readonly sessaoService = inject(Sessao);
  private readonly router = inject(Router);

  /** Query params (`withComponentInputBinding`). */
  readonly destino = input<string>();
  readonly sessao = input<string>();

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly mostrarSenha = signal(false);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    cpf: ['', [Validators.required, validadorCpf]],
    senha: ['', Validators.required],
  });

  protected entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { cpf, senha } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.erro.set(null);
    this.sessaoService.login(cpf, senha).subscribe({
      next: () => void this.router.navigateByUrl(this.destinoSeguro()),
      error: (erro: unknown) => {
        this.erro.set(mensagemDeErro(erro));
        this.enviando.set(false);
      },
    });
  }

  /** Só caminhos internos: um `destino=https://...` vindo de link externo é ignorado (open redirect). */
  private destinoSeguro(): string {
    const destino = this.destino();
    return destino && destino.startsWith('/') && !destino.startsWith('//') ? destino : '/inicio';
  }
}
