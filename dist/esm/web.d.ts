import { WebPlugin } from '@capacitor/core';
import type { SDKInfo, SDKInfoPlugin } from './definitions';
export declare class SDKInfoPluginWeb extends WebPlugin implements SDKInfoPlugin {
    getSDKInfo(): Promise<SDKInfo>;
    unregisterAppFeature(options: {
        feature: string;
    }): Promise<void>;
    registerAppFeature(options: {
        feature: string;
    }): Promise<void>;
}
