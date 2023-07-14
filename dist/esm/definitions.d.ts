export interface SDKInfo {
    sdkVersion: string;
    appName: string;
    appVersion: string;
    forcePluginsAvailable: Array<string>;
    bootConfig: string;
}
export interface SDKInfoPlugin {
    getSDKInfo(): Promise<SDKInfo>;
    unregisterAppFeature(options: {
        feature: string;
    }): Promise<void>;
    registerAppFeature(options: {
        feature: string;
    }): Promise<void>;
}
