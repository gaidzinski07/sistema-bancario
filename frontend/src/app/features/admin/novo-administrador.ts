import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { AdminApi } from '../../core/api/admin-api';
import { PerfilAdministrador } from '../../core/api/modelos';
import { Notificacoes } from '../../core/erros/notificacoes';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';
import { digitosDoCpf, validadorCpf } from '../../shared/cpf';

@Component({
  selector: 'app-novo-administrador',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatProgressBarModule],
  template: `
    <h1>Novo administrador</h1>
    @if (enviando()) {
      <mat-progress-bar mode="indeterminate" />
    }
    <form [formGroup]="formulario" (ngSubmit)="salvar()" class="formulario">
      <mat-form-field>
        <mat-label>Nome completo</mat-label>
        <input matInput formControlName="nome" />
        @if (formulario.controls.nome.hasError('required')) { <mat-error>Informe o nome.</mat-error> }
      </mat-form-field>
      <mat-form-field>
        <mat-label>CPF</mat-label>
        <input matInput formControlName="cpf" inputmode="numeric" placeholder="000.000.000-00" />
        @if (formulario.controls.cpf.hasError('required')) { <mat-error>Informe o CPF.</mat-error> }
        @else if (formulario.controls.cpf.hasError('cpf')) { <mat-error>CPF inválido.</mat-error> }
      </mat-form-field>
      <mat-form-field>
        <mat-label>E-mail</mat-label>
        <input matInput type="email" formControlName="email" />
      </mat-form-field>
      <mat-form-field>
        <mat-label>Perfil</mat-label>
        <mat-select formControlName="perfil">
          <mat-option value="OPERADOR">Operador</mat-option>
          <mat-option value="GERENTE">Gerente</mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field>
        <mat-label>Senha inicial</mat-label>
        <input matInput type="password" formControlName="senha" autocomplete="new-password" />
        @if (formulario.controls.senha.hasError('minlength')) { <mat-error>Mínimo de 8 caracteres.</mat-error> }
        @else if (formulario.controls.senha.hasError('required')) { <mat-error>Informe a senha.</mat-error> }
      </mat-form-field>
      @if (erro()) {
        <p class="erro" role="alert">{{ erro() }}</p>
      }
      <div class="acoes">
        <button mat-flat-button type="submit" [disabled]="enviando()">Cadastrar</button>
      </div>
    </form>
  `,
  styles: `
    .formulario { display: flex; flex-direction: column; gap: 12px; max-width: 440px; }
    .acoes { display: flex; justify-content: flex-end; }
    .erro { color: var(--mat-sys-error); margin: 0; }
  `,
})
export class NovoAdministrador {
  private readonly api = inject(AdminApi);
  private readonly notificacoes = inject(Notificacoes);

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly formulario = inject(NonNullableFormBuilder).group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    cpf: ['', [Validators.required, validadorCpf]],
    email: ['', Validators.email],
    perfil: ['OPERADOR' as PerfilAdministrador, Validators.required],
    senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  protected salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    this.enviando.set(true);
    this.erro.set(null);
    this.api
      .cadastrarAdministrador({ nome: v.nome.trim(), cpf: digitosDoCpf(v.cpf), email: v.email.trim() || null, senha: v.senha, perfil: v.perfil })
      .subscribe({
        next: (admin) => {
          this.enviando.set(false);
          this.formulario.reset();
          this.notificacoes.sucesso(`Administrador ${admin.nome} cadastrado.`);
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
