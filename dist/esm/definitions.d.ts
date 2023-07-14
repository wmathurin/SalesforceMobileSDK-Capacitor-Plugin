export interface SDKInfo {
    sdkVersion: string;
    appName: string;
    appVersion: string;
    forcePluginsAvailable: string[];
    bootConfig: string;
}
export interface SDKInfoPlugin {
    getInfo(): Promise<SDKInfo>;
    unregisterAppFeature(options: {
        feature: string;
    }): Promise<void>;
    registerAppFeature(options: {
        feature: string;
    }): Promise<void>;
}
