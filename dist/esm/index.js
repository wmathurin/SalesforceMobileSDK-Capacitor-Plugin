import { registerPlugin } from '@capacitor/core';
const sdkInfoPluginRegistered = registerPlugin('SDKInfoPlugin', {
    web: () => import('./web').then(m => new m.SDKInfoPluginWeb()),
});
const networkPluginRegistered = registerPlugin('SalesforceNetworkPlugin', {
    web: () => import('./web').then(m => new m.SalesforceNetworkPluginWeb()),
});
export * from './definitions';
export { sdkInfoPluginRegistered as SDKInfoPlugin };
export { networkPluginRegistered as SalesforceNetworkPlugin };
//# sourceMappingURL=index.js.map