import { WebPlugin } from '@capacitor/core';
export class SDKInfoPluginWeb extends WebPlugin {
    async getSDKInfo() {
        console.log("Called getSDKInfo");
        return new Promise((resolve, reject) => {
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
        console.log("Called unregisterAppFeature");
        return new Promise((resolve, reject) => {
            resolve();
        });
    }
    async registerAppFeature(options) {
        console.log("Called registerAppFeature");
        return new Promise((resolve, reject) => {
            resolve();
        });
    }
}
//# sourceMappingURL=web.js.map