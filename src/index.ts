import { registerPlugin } from '@capacitor/core';

import type { SDKInfoPlugin, SalesforceNetworkPlugin } from './definitions';

const sdkInfoPluginRegistered  = registerPlugin<SDKInfoPlugin>(
  'SDKInfoPlugin',
  {
    web: () => import('./web').then(m => new m.SDKInfoPluginWeb()),
  },
);

const networkPluginRegistered  = registerPlugin<SalesforceNetworkPlugin>(
  'SalesforceNetworkPlugin',
  {
    web: () => import('./web').then(m => new m.SalesforceNetworkPluginWeb()),
  },
);


export * from './definitions';
export { sdkInfoPluginRegistered as SDKInfoPlugin };
export { networkPluginRegistered as SalesforceNetworkPlugin }
