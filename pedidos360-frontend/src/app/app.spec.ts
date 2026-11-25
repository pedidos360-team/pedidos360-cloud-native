import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { AppComponent } from './app';

describe('AppComponent', () => {
  beforeEach(async () => {
    const msalService = {
      instance: {
        initializeWrapperLibrary: () => undefined,
        initialize: () => Promise.resolve(),
        handleRedirectPromise: () => Promise.resolve(null),
        getAllAccounts: () => [],
        setActiveAccount: () => undefined
      }
    };

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        { provide: MsalService, useValue: msalService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
