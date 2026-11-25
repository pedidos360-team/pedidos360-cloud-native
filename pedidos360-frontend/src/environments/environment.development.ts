export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8081',
  ordersApiBaseUrl: 'http://localhost:8082',
  azure: {
    clientId: 'c6f45fc0-a444-4879-8ddb-70b6cee9ecc5',
    tenantId: 'cb565889-45aa-446c-b565-301e6a79000f',
    authority: 'https://login.microsoftonline.com/cb565889-45aa-446c-b565-301e6a79000f',
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200',
    loginScopes: ['openid', 'profile', 'email'],
    // Debe coincidir con el scope delegado creado en Entra ID > Expose an API.
    apiScopes: ['api://c6f45fc0-a444-4879-8ddb-70b6cee9ecc5/access_as_user']
  }
};