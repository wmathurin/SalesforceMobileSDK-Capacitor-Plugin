'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const sdkInfoPluginRegistered = core.registerPlugin('SDKInfoPlugin', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.SDKInfoPluginWeb()),
});
const networkPluginRegistered = core.registerPlugin('SalesforceNetworkPlugin', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.SalesforceNetworkPluginWeb()),
});

class SDKInfoPluginWeb extends core.WebPlugin {
    async getInfo() {
        console.log("Called getInfo");
        return new Promise((resolve) => {
            resolve({
                sdkVersion: "Sdk-Version",
                appName: "App-Name",
                appVersion: "1.0.0",
                forcePluginsAvailable: [],
                bootConfig: ""
            });
        });
    }
    async unregisterAppFeature(options) {
        console.log(`Called unregisterAppFeature ${options.feature}`);
        return new Promise((resolve) => {
            resolve();
        });
    }
    async registerAppFeature(options) {
        console.log(`Called registerAppFeature ${options.feature}`);
        return new Promise((resolve) => {
            resolve();
        });
    }
}
class SalesforceNetworkPluginWeb extends core.WebPlugin {
    async sendRequest(options) {
        console.log("Called sendRequest");
        return new Promise((resolve) => {
            resolve({
                body: `response for ${options.path}`
            });
        });
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    SDKInfoPluginWeb: SDKInfoPluginWeb,
    SalesforceNetworkPluginWeb: SalesforceNetworkPluginWeb
});

exports.SDKInfoPlugin = sdkInfoPluginRegistered;
exports.SalesforceNetworkPlugin = networkPluginRegistered;
//# sourceMappingURL=plugin.cjs.js.map
