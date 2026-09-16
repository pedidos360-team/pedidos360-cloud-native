export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8081',
  azure: {
    clientId: 'ed614ce8-0c2e-4a39-b0c0-546b14f540df',
    tenantId: 'f526ccbd-b7aa-4640-a83e-9c33ff4bc07e',
    authority: 'https://login.microsoftonline.com/f526ccbd-b7aa-4640-a83e-9c33ff4bc07e',
    redirectUri: 'http://localhost:4200',
    protectedResourceScopes: ['user.read']
  }
};