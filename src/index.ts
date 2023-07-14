import { registerPlugin } from '@capacitor/core';

import type { SDKInfoPlugin } from './definitions';

const registeredPlugin  = registerPlugin<SDKInfoPlugin>(
  'SDKInfoPlugin',
  {
    web: () => import('./web').then(m => new m.SDKInfoPluginWeb()),
  },
);

export * from './definitions';
export { registeredPlugin as SDKInfoPlugin };
