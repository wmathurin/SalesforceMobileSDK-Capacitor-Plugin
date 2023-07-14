import { registerPlugin } from '@capacitor/core';
const registeredPlugin = registerPlugin('SDKInfoPlugin', {
    web: () => import('./web').then(m => new m.SDKInfoPluginWeb()),
});
export * from './definitions';
export { registeredPlugin as SDKInfoPlugin };
//# sourceMappingURL=index.js.map