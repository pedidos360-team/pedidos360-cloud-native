export const environment = {
  production: true,
  apiBaseUrl: 'https://cy7ptfnq81.execute-api.us-east-1.amazonaws.com',
  ordersApiBaseUrl: 'https://cy7ptfnq81.execute-api.us-east-1.amazonaws.com',
  azure: {
    clientId: 'ed614ce8-0c2e-4a39-b0c0-546b14f540df',
    tenantId: 'f526ccbd-b7aa-4640-a83e-9c33ff4bc07e',
    authority: 'https://login.microsoftonline.com/f526ccbd-b7aa-4640-a83e-9c33ff4bc07e',
    redirectUri: 'https://frontend-aws.dlp7esd3jan5z.amplifyapp.com',
    postLogoutRedirectUri: 'https://frontend-aws.dlp7esd3jan5z.amplifyapp.com',
    loginScopes: ['openid', 'profile', 'email'],
    apiScopes: ['api://ed614ce8-0c2e-4a39-b0c0-546b14f540df/access_as_user']
  }
};