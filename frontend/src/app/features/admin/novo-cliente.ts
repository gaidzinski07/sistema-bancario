import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/api/admin-api';
import { Endereco, SegmentoCliente } from '../../core/api/modelos';
import { Notificacoes } from '../../core/erros/notificacoes';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';
import { digitosDoCpf, validadorCpf } from '../../shared/cpf';
import { dataIso } from '../../shared/formatos';

/** Cadastro de cliente: cria o usuário (login por CPF + senha) e o cadastro de cliente. */
@Component({
  selector: 'app-novo-cliente',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatSelectModule, MatDatepickerModule,
    MatButtonModule, MatProgressBarModule],
  template: `
    <h1>Novo cliente</h1>
    @if (enviando()) {
      <mat-progress-bar mode="indeterminate" />
    }
    <form [formGroup]="formulario" (ngSubmit)="salvar()" class="formulario">
      <h2>Dados pessoais</h2>
      <div class="grade">
        <mat-form-field class="largo">
          <mat-label>Nome completo</mat-label>
          <input matInput formControlName="nome" autocomplete="off" />
          @if (formulario.controls.nome.hasError('required')) { <mat-error>Informe o nome.</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>CPF</mat-label>
          <input matInput formControlName="cpf" inputmode="numeric" placeholder="000.000.000-00" />
          @if (formulario.controls.cpf.hasError('required')) { <mat-error>Informe o CPF.</mat-error> }
          @else if (formulario.controls.cpf.hasError('cpf')) { <mat-error>CPF inválido.</mat-error> }
          @else if (formulario.controls.cpf.hasError('servidor')) { <mat-error>{{ formulario.controls.cpf.getError('servidor') }}</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Data de nascimento</mat-label>
          <input matInput [matDatepicker]="nascimento" formControlName="dataNascimento" [max]="hoje" />
          <mat-datepicker-toggle matIconSuffix [for]="nascimento" />
          <mat-datepicker #nascimento startView="multi-year" />
        </mat-form-field>
        <mat-form-field class="largo">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="off" />
          @if (formulario.controls.email.hasError('email')) { <mat-error>E-mail inválido.</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Segmento</mat-label>
          <mat-select formControlName="segmento">
            <mat-option value="CORRENTISTA">Correntista</mat-option>
            <mat-option value="INVESTIDOR">Investidor</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Senha inicial</mat-label>
          <input matInput type="password" formControlName="senha" autocomplete="new-password" />
          <mat-hint>De 8 a 72 caracteres. Repasse ao cliente com segurança.</mat-hint>
          @if (formulario.controls.senha.hasError('required')) { <mat-error>Informe a senha.</mat-error> }
          @else if (formulario.controls.senha.hasError('minlength')) { <mat-error>Mínimo de 8 caracteres.</mat-error> }
        </mat-form-field>
      </div>

      <h2>Endereço <span class="opcional">(opcional)</span></h2>
      <div class="grade" formGroupName="endereco">
        <mat-form-field class="largo"><mat-label>Logradouro</mat-label><input matInput formControlName="logradouro" /></mat-form-field>
        <mat-form-field><mat-label>Número</mat-label><input matInput formControlName="numero" /></mat-form-field>
        <mat-form-field><mat-label>Complemento</mat-label><input matInput formControlName="complemento" /></mat-form-field>
        <mat-form-field><mat-label>Bairro</mat-label><input matInput formControlName="bairro" /></mat-form-field>
        <mat-form-field><mat-label>Cidade</mat-label><input matInput formControlName="cidade" /></mat-form-field>
        <mat-form-field class="curto">
          <mat-label>UF</mat-label><input matInput formControlName="uf" maxlength="2" />
          @if (formulario.controls.endereco.controls.uf.invalid) { <mat-error>2 letras.</mat-error> }
        </mat-form-field>
        <mat-form-field>
          <mat-label>CEP</mat-label><input matInput formControlName="cep" inputmode="numeric" placeholder="00000-000" />
          @if (formulario.controls.endereco.controls.cep.invalid) { <mat-error>Formato 00000-000.</mat-error> }
        </mat-form-field>
      </div>

      @if (erro()) {
        <p class="erro" role="alert">{{ erro() }}</p>
      }
      <div class="acoes">
        <a mat-button routerLink="/admin/clientes">Cancelar</a>
        <button mat-flat-button type="submit" [disabled]="enviando()">Cadastrar</button>
      </div>
    </form>
  `,
  styles: `
    .formulario { max-width: 760px; }
    .grade { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px 16px; }
    .largo { grid-column: span 2; }
    @media (max-width: 599px) { .largo { grid-column: auto; } }
    .opcional { font: var(--mat-sys-body-medium); color: var(--mat-sys-on-surface-variant); }
    .acoes { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class NovoCliente {
  private readonly api = inject(AdminApi);
  private readonly router = inject(Router);
  private readonly notificacoes = inject(Notificacoes);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly hoje = new Date();
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    cpf: ['', [Validators.required, validadorCpf]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    dataNascimento: [null as Date | null],
    segmento: ['CORRENTISTA' as SegmentoCliente, Validators.required],
    senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    endereco: this.fb.group({
      logradouro: [''],
      numero: [''],
      complemento: [''],
      bairro: [''],
      cidade: [''],
      uf: ['', Validators.pattern(/^[A-Za-z]{2}$/)],
      cep: ['', Validators.pattern(/^\d{5}-?\d{3}$/)],
    }),
  });

  protected salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const v = this.formulario.getRawValue();
    const endereco = Object.values(v.endereco).some((campo) => campo.trim())
      ? (Object.fromEntries(Object.entries(v.endereco).map(([k, valor]) => [k, valor.trim() || null])) as Endereco)
      : null;

    this.enviando.set(true);
    this.erro.set(null);
    this.api
      .cadastrarCliente({
        nome: v.nome.trim(),
        cpf: digitosDoCpf(v.cpf),
        email: v.email.trim() || null,
        dataNascimento: v.dataNascimento ? dataIso(v.dataNascimento) : null,
        senha: v.senha,
        segmento: v.segmento,
        endereco,
      })
      .subscribe({
        next: (cliente) => {
          this.notificacoes.sucesso(`Cliente ${cliente.nome} cadastrado. Abra uma conta para ele.`);
          void this.router.navigate(['/admin/clientes', cliente.id]);
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
