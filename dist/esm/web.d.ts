import { WebPlugin } from '@capacitor/core';
import type { RestRequest, RestResponse, SDKInfo, SDKInfoPlugin, SalesforceNetworkPlugin } from './definitions';
export declare class SDKInfoPluginWeb extends WebPlugin implements SDKInfoPlugin {
    getInfo(): Promise<SDKInfo>;
    unregisterAppFeature(options: {
        feature: string;
    }): Promise<void>;
    registerAppFeature(options: {
        feature: string;
    }): Promise<void>;
}
export declare class SalesforceNetworkPluginWeb extends WebPlugin implements SalesforceNetworkPlugin {
    sendRequest(options: RestRequest): Promise<RestResponse>;
}
