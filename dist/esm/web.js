import { WebPlugin } from '@capacitor/core';
export class SDKInfoPluginWeb extends WebPlugin {
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
export class SalesforceNetworkPluginWeb extends WebPlugin {
    async sendRequest(options) {
        console.log("Called sendRequest");
        return new Promise((resolve) => {
            resolve({
                body: `response for ${options.path}`
            });
        });
    }
}
//# sourceMappingURL=web.js.map