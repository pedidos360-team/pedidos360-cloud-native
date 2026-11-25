export const environment = {
  production: true,
  apiBaseUrl: 'https://bnatagmjye.execute-api.us-east-1.amazonaws.com/',
  ordersApiBaseUrl: 'https://bnatagmjye.execute-api.us-east-1.amazonaws.com/',
  azure: {
    clientId: 'c6f45fc0-a444-4879-8ddb-70b6cee9ecc5',
    tenantId: 'cb565889-45aa-446c-b565-301e6a79000f',
    authority: 'https://login.microsoftonline.com/cb565889-45aa-446c-b565-301e6a79000f',
    redirectUri: 'https://TU_DOMINIO.amplifyapp.com',
    postLogoutRedirectUri: 'https://TU_DOMINIO.amplifyapp.com',
    loginScopes: ['openid', 'profile', 'email'],
    apiScopes: ['api://c6f45fc0-a444-4879-8ddb-70b6cee9ecc5/access_as_user']
  }
};