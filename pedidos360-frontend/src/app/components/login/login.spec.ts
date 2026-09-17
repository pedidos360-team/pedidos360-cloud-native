import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MsalService } from '@azure/msal-angular';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    const msalService = {
      instance: {
        getAllAccounts: () => [],
        getActiveAccount: () => null
      },
      loginRedirect: () => ({ subscribe: () => undefined }),
      logoutRedirect: () => ({ subscribe: () => undefined })
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [{ provide: MsalService, useValue: msalService }]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
