import Foundation
import Capacitor
import SalesforceSDKCore

@objc (SDKInfoPlugin)
public class SDKInfoPlugin: CAPPlugin {
    
    @objc public func getInfo(_ call: CAPPluginCall) {
        call.resolve(SDKInfo.getSDKInfo().dictionary)
    }
    
    @objc public func registerAppFeature(_ call: CAPPluginCall) {
        let appFeatureCode = call.getString("feature") ?? ""
        if (!appFeatureCode.isEmpty) {
            SFSDKAppFeatureMarkers.registerAppFeature(appFeatureCode)
        }
        call.resolve()
    }

    @objc public func unregisterAppFeature(_ call: CAPPluginCall) {
        let appFeatureCode = call.getString("feature") ?? ""
        if (!appFeatureCode.isEmpty) {
            SFSDKAppFeatureMarkers.unregisterAppFeature(appFeatureCode)
        }
        call.resolve()
    }
}

struct SDKInfo {
    let sdkVersion: String
    let appName:String
    let appVersion: String
    let forcePluginsAvailable: [String]
    let bootConfig: String
    
    var dictionary: [String: Any] {
        return [
            "sdkVersion": sdkVersion,
            "appName": appName,
            "appVersion": appVersion,
            "forcePluginsAvailable": forcePluginsAvailable,
            "bootConfig": bootConfig
        ]
    }
    
    static func getSDKInfo() -> SDKInfo {
        let appName = getBundleValue(kCFBundleNameKey as String)
        let appVersion = getBundleValue(kCFBundleVersionKey as String)
        
        return SDKInfo(
            sdkVersion: "", // fixme
            appName: appName,
            appVersion: appVersion,
            forcePluginsAvailable: [], // fixme
            bootConfig: "" // fixme
        )
    }
    
    static func getBundleValue(_ key: String) -> String {
        return Bundle.main.infoDictionary?[key] as? String ?? ""
    }
}
