import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { App } from './app';
describe('App',()=>{beforeEach(async()=>TestBed.configureTestingModule({imports:[App],providers:[provideHttpClient()]}).compileComponents());it('creates the dashboard',()=>expect(TestBed.createComponent(App).componentInstance).toBeTruthy());it('starts with a zero aggregate balance',()=>expect(TestBed.createComponent(App).componentInstance.totalBalance()).toBe(0));});
