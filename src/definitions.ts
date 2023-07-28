export interface SDKInfo {
    sdkVersion: string;
    appName: string;
    appVersion: string;
    forcePluginsAvailable: string[];
    bootConfig: string;
}

export interface SDKInfoPlugin {
    getInfo(): Promise<SDKInfo>;
    unregisterAppFeature(options: { feature: string }): Promise<void>;
    registerAppFeature(options: { feature: string }): Promise<void>;  
}

export interface RestRequest {
    method: String;
    endPoint: String;
    path: String;
    queryParams?: String;
    headerParams?: Map<String, String>;
    fileParams?: Map<String, String>;
  }

export interface RestResponse {
    headers?: Map<String, String>;
    body: String;
}

export interface SalesforceNetworkPlugin {
    sendRequest(options: RestRequest): Promise<RestResponse>;
}