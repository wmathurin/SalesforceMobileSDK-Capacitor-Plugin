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

package com.salesforce.androidsdk.capacitor.app

import android.app.Activity
import android.content.Context
import com.salesforce.androidsdk.capacitor.ui.SalesforceBridgeActivity
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.ui.LoginActivity
import com.salesforce.androidsdk.util.EventsObservable


/**
 * SDK Manager for all hybrid applications.
 */
class SalesforceHybridSDKManager
/**
 * Protected constructor.
 *
 * @param context Application context.
 * @param mainActivity Activity that should be launched after the login flow.
 * @param loginActivity Login activity.
 */
protected constructor(
    context: Context?, mainActivity: Class<out Activity?>?,
    loginActivity: Class<out Activity?>?
) :
    MobileSyncSDKManager(context, mainActivity, loginActivity) {


    override fun getAppType(): String {
        return "Hybrid"
    }

    override fun isHybrid(): Boolean {
        return true
    }

    companion object {
        private const val TAG = "SalesforceHybridSDKManager"
        private fun init(
            context: Context, mainActivity: Class<out Activity?>,
            loginActivity: Class<out Activity?>
        ) {
            if (INSTANCE == null) {
                INSTANCE = SalesforceHybridSDKManager(context, mainActivity, loginActivity)
            }

            initInternal(context)
            EventsObservable.get().notifyEvent(EventsObservable.EventType.AppCreateComplete)
        }

        /**
         * Initializes components required for this class
         * to properly function. This method should be called
         * by hybrid apps using the Salesforce Mobile SDK.
         *
         * @param context Application context.
         */
        fun initHybrid(context: Context) {
            init(
                context, SalesforceBridgeActivity::class.java,
                LoginActivity::class.java
            )
        }


        /**
         * Returns a singleton instance of this class.
         *
         * @return Singleton instance of SalesforceHybridSDKManager.
         */
        val instance: SalesforceHybridSDKManager
            get() = if (INSTANCE != null) {
                INSTANCE as SalesforceHybridSDKManager
            } else {
                throw RuntimeException("Applications need to call SalesforceHybridSDKManager.init() first.")
            }
    }
}