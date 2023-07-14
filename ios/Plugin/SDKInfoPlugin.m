#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(SDKInfoPlugin, "SDKInfoPlugin",
           CAP_PLUGIN_METHOD(getInfo, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(registerAppFeature, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(unregisterAppFeature, CAPPluginReturnPromise);
)
