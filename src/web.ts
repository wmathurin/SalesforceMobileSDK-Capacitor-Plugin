import { WebPlugin } from '@capacitor/core';

import type { SDKInfo, SDKInfoPlugin } from './definitions';

export class SDKInfoPluginWeb
extends WebPlugin
implements SDKInfoPlugin
{
    async getInfo(): Promise<SDKInfo> {
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
    
    async unregisterAppFeature(options: { feature: string }): Promise<void> {
	console.log(`Called unregisterAppFeature ${options.feature}`);
	return new Promise((resolve) => {
	    resolve();
	});
    }	

    async registerAppFeature(options: { feature: string }): Promise<void> {
	console.log(`Called registerAppFeature ${options.feature}`);
	return new Promise((resolve) => {
	    resolve();
	});
    }
}
