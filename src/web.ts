import { WebPlugin } from '@capacitor/core';

import type { SDKInfo, SDKInfoPlugin } from './definitions';

export class SDKInfoPluginWeb
extends WebPlugin
implements SDKInfoPlugin
{
    async getSDKInfo(): Promise<SDKInfo> {
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
    
    async unregisterAppFeature(options: { feature: string }): Promise<void> {
	console.log("Called unregisterAppFeature");
	return new Promise((resolve, reject) => {
	    resolve();
	});
    }	

    async registerAppFeature(options: { feature: string }): Promise<void> {
	console.log("Called registerAppFeature");
	return new Promise((resolve, reject) => {
	    resolve();
	});
    }
}
