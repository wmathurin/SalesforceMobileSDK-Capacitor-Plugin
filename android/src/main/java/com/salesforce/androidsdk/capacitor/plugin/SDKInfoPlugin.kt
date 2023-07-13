/*
 * Copyright (c) 2023-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.androidsdk.capacitor.plugin

import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@CapacitorPlugin(name = "SDKInfoPlugin")
class SDKInfoPlugin : Plugin() {

    @PluginMethod
    fun getInfo(call: PluginCall) {
        SalesforceHybridLogger.i(TAG, "getInfo called")
        val info = SDKInfo.getSDKInfo(context)
        call.resolve(info.toJSObject())
    }

    @PluginMethod
    fun registerAppFeature(call: PluginCall) {
        SalesforceHybridLogger.i(TAG, "registerAppFeature called")
        val appFeatureCode = call.getString("feature")
        if (!appFeatureCode.isNullOrEmpty()) {
            SalesforceSDKManager.getInstance().registerUsedAppFeature(appFeatureCode)
        }
        call.resolve()
    }

    @PluginMethod
    fun unregisterAppFeature(call: PluginCall) {
        SalesforceHybridLogger.i(TAG, "unregisterAppFeature called")
        val appFeatureCode = call.getString("feature")
        if (!appFeatureCode.isNullOrEmpty()) {
            SalesforceSDKManager.getInstance().unregisterUsedAppFeature(appFeatureCode)
        }
        call.resolve()
    }

    companion object {
        private const val TAG = "SDKInfoPlugin"
    }
}

@Serializable
internal data class SDKInfo (
    val sdkVersion: String,
    val appName:String,
    val appVersion: String,
    val forcePluginsAvailable: List<String>,
    val bootConfig: String,
) {

    fun toJSObject(): JSObject = JSObject(Json.encodeToString(this))

    companion object {
        fun getSDKInfo(ctx: Context): SDKInfo {
            return SDKInfo(
                SalesforceSDKManager.SDK_VERSION,
                "", // fixme
                SalesforceSDKManager.getInstance().appVersion,
                emptyList(), // fixme
                "", // fixme
            )
        }
    }
}
