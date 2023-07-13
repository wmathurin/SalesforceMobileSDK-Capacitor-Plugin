import { registerPlugin } from '@capacitor/core';

import type { CapacitorPluginPlugin } from './definitions';

const CapacitorPlugin = registerPlugin<CapacitorPluginPlugin>(
  'CapacitorPlugin',
  {
    web: () => import('./web').then(m => new m.CapacitorPluginWeb()),
  },
);

export * from './definitions';
export { CapacitorPlugin };
