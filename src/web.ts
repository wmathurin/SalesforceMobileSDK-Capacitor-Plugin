import { WebPlugin } from '@capacitor/core';

import type { CapacitorPluginPlugin } from './definitions';

export class CapacitorPluginWeb
  extends WebPlugin
  implements CapacitorPluginPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
