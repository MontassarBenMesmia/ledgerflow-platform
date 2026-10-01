import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import Keycloak from 'keycloak-js';
import { forkJoin } from 'rxjs';

interface Account { id: string; name: string; currency: string; balance: number; }
interface Activity { id: string; sourceAccountId: string; destinationAccountId: string; amount: number; currency: string; initiatedBy: string; occurredAt: string; reconciliationStatus: string; }
interface Reconciliation { status: string; processedEvents: number; projectedTransfers: number; balancedTransfers: number; reviewRequired: number; checkedAt: string; }

@Component({ imports: [CommonModule, ReactiveFormsModule], selector: 'app-root', styleUrl: './app.scss', templateUrl: './app.html' })
export class App implements OnInit {
  private readonly keycloak = new Keycloak({ url: window.location.origin, realm: 'ledgerflow', clientId: 'ledgerflow-web' });
  readonly accounts = signal<Account[]>([]);
  readonly activity = signal<Activity[]>([]);
  readonly reconciliation = signal<Reconciliation | null>(null);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly message = signal('');
  readonly user = signal('Operator');
  readonly totalBalance = computed(() => this.accounts().reduce((sum, account) => sum + Number(account.balance), 0));
  readonly form = new FormGroup({
    sourceAccountId: new FormControl('', { nonNullable: true, validators: Validators.required }),
    destinationAccountId: new FormControl('', { nonNullable: true, validators: Validators.required }),
    amount: new FormControl(250, { nonNullable: true, validators: [Validators.required, Validators.min(0.01)] }),
  });

  constructor(private readonly http: HttpClient) {}

  async ngOnInit(): Promise<void> {
    try {
      const authenticated = await this.keycloak.init({ onLoad: 'login-required', checkLoginIframe: false, pkceMethod: 'S256' });
      if (authenticated) {
        this.user.set(this.keycloak.tokenParsed?.['preferred_username'] ?? 'Operator');
        this.refresh();
      }
    } catch {
      this.message.set('Identity service is unavailable. Check the local platform status.');
      this.loading.set(false);
    }
  }

  private headers(extra: Record<string, string> = {}): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.keycloak.token}`, ...extra });
  }

  refresh(): void {
    this.loading.set(true);
    forkJoin({
      accounts: this.http.get<Account[]>('/api/ledger/accounts', { headers: this.headers() }),
      activity: this.http.get<Activity[]>('/api/projection/activity', { headers: this.headers() }),
      reconciliation: this.http.get<Reconciliation>('/api/projection/reconciliation', { headers: this.headers() }),
    }).subscribe({
      next: ({ accounts, activity, reconciliation }) => {
        this.accounts.set(accounts); this.activity.set(activity); this.reconciliation.set(reconciliation);
        this.selectDefaults(); this.loading.set(false);
      },
      error: () => { this.message.set('Unable to refresh platform data.'); this.loading.set(false); },
    });
  }

  private selectDefaults(): void {
    if (!this.form.controls.sourceAccountId.value && this.accounts().length > 1) {
      this.form.patchValue({ sourceAccountId: this.accounts()[0].id, destinationAccountId: this.accounts()[1].id });
    }
  }

  submitTransfer(): void {
    if (this.form.invalid || this.form.value.sourceAccountId === this.form.value.destinationAccountId) {
      this.message.set('Choose two different accounts and a positive amount.'); return;
    }
    this.submitting.set(true); this.message.set('');
    this.http.post('/api/ledger/transfers', { ...this.form.getRawValue(), currency: 'EUR' }, {
      headers: this.headers({ 'Idempotency-Key': crypto.randomUUID() }),
    }).subscribe({
      next: () => { this.message.set('Transfer posted. The event projection will update momentarily.'); this.submitting.set(false); setTimeout(() => this.refresh(), 900); },
      error: (error) => { this.message.set(error.error?.detail ?? 'Transfer could not be posted.'); this.submitting.set(false); },
    });
  }

  accountName(id: string): string { return this.accounts().find(account => account.id === id)?.name ?? 'Account'; }
  initials(): string { return this.user().split(/[._\s-]/).map(value => value[0]).join('').slice(0, 2).toUpperCase(); }
  logout(): void { this.keycloak.logout({ redirectUri: window.location.origin }); }
}
