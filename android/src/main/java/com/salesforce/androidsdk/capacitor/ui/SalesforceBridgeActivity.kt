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

package com.salesforce.androidsdk.capacitor.ui

import android.os.Bundle
import android.view.KeyEvent
import com.getcapacitor.BridgeActivity
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.auth.HttpAccess.NoNetworkException
import com.salesforce.androidsdk.capacitor.app.SalesforceHybridSDKManager
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger.i
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger.w
import com.salesforce.androidsdk.config.BootConfig
import com.salesforce.androidsdk.config.BootConfig.BootConfigException
import com.salesforce.androidsdk.rest.ApiVersionStrings
import com.salesforce.androidsdk.rest.ClientManager
import com.salesforce.androidsdk.rest.ClientManager.AccountInfoNotFoundException
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback
import com.salesforce.androidsdk.rest.RestRequest
import com.salesforce.androidsdk.rest.RestRequest.RestMethod
import com.salesforce.androidsdk.rest.RestRequest.UTF_8
import com.salesforce.androidsdk.rest.RestResponse
import com.salesforce.androidsdk.ui.SalesforceActivityDelegate
import com.salesforce.androidsdk.ui.SalesforceActivityInterface
import com.salesforce.androidsdk.util.AuthConfigUtil.MyDomainAuthConfig
import java.net.URLEncoder


class SalesforceBridgeActivity : BridgeActivity(), SalesforceActivityInterface {

    companion object {
        const val TAG = "SalesforceBridgeActivity"

        fun getRequestForLimits(apiVersion: String): RestRequest {
            val path = String.format("/services/data/%s/limits", apiVersion)
            return RestRequest(RestMethod.GET, path)
        }
    }


    // Delegate
    private val delegate = SalesforceActivityDelegate(this)

    // Rest client
    private lateinit var clientManager: ClientManager
    private var client: RestClient? = null

    // Config
    private lateinit var bootconfig: BootConfig
    private val authConfig: MyDomainAuthConfig? = null

    // Web app loaded?
    private var webAppLoaded = false

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get bootconfig
        bootconfig = BootConfig.getBootConfig(this)

        // Get clientManager
        clientManager = buildClientManager()

        // Setup global stores and syncs defined in static configs
        SalesforceHybridSDKManager.instance.setupGlobalStoreFromDefaultConfig()
        SalesforceHybridSDKManager.instance.setupGlobalSyncsFromDefaultConfig()

        // Delegate create
        delegate.onCreate()
    }

    fun buildClientManager(): ClientManager {
        return SalesforceHybridSDKManager.instance.clientManager
    }

    override fun onResume() {
        super.onResume()

        // FIXME

//        // Fetches auth config if required.
//        try {
//            FetchAuthConfigTask().execute()
//        } catch (e: Exception) {
//            e(TAG, "Exception occurred while fetching auth config", e)
//        }

        delegate.onResume(false)
        // will call this.onResume(client:RestClient) with a null client
    }

    override fun onResume(c: RestClient?) {
        // Called from delegate with null

        // Get client (if already logged in)
        client = try {
            clientManager.peekRestClient()
        } catch (e: AccountInfoNotFoundException) {
            null
        }

        // Not logged in
        if (client == null) {
            if (!webAppLoaded) {
                onResumeNotLoggedIn()
            } else {
                i(TAG, "onResume - unauthenticated web app already loaded")
            }
        } else {

            // Web app never loaded
            if (!webAppLoaded) {
                onResumeLoggedInNotLoaded()
            } else {
                i(TAG, "onResume - already logged in/web app already loaded")
            }
        }
    }

    /**
     * Returns the auth config associated with the current login server, if it exists.
     *
     * @return Auth config.
     */
    fun getAuthConfig(): MyDomainAuthConfig? {
        return authConfig
    }

    /**
     * Called when resuming activity and user is not authenticated
     */
    private fun onResumeNotLoggedIn() {
        try {
            BootConfig.validateBootConfig(bootconfig)

            // Need to be authenticated
            if (bootconfig.shouldAuthenticate()) {

                // Online
                if (SalesforceSDKManager.getInstance().hasNetwork()) {
                    i(TAG, "onResumeNotLoggedIn - should authenticate/online - authenticating")
                    authenticate(/*null*/)
                } else {
                    w(TAG, "onResumeNotLoggedIn - should authenticate/offline - can not proceed")
                    loadErrorPage()
                }
            } else {

                // Local
                if (bootconfig.isLocal) {
                    i(
                        TAG,
                        "onResumeNotLoggedIn - should not authenticate/local start page - loading web app"
                    )
                    loadLocalStartPage()
                } else {
                    w(
                        TAG,
                        "onResumeNotLoggedIn - should not authenticate/remote start page - loading web app"
                    )
                    loadRemoteStartPage(getUnauthenticatedStartPage(), false)
                }
            }
        } catch (e: BootConfigException) {
            w(
                TAG, "onResumeNotLoggedIn - Boot config did not pass validation: "
                        + e.message
                        + " - cannot proceed"
            )
            loadErrorPage()
        }
    }

    /**
     * Called when resuming activity and user is authenticated but webview has not been loaded yet
     */
    private fun onResumeLoggedInNotLoaded() {

        // Setup user stores and syncs defined in static configs
        SalesforceHybridSDKManager.instance.setupUserStoreFromDefaultConfig()
        SalesforceHybridSDKManager.instance.setupUserSyncsFromDefaultConfig()

        // Local
        if (bootconfig.isLocal) {
            i(TAG, "onResumeLoggedInNotLoaded - local start page - loading web app")
            loadLocalStartPage()
        } else {

            // Online
            if (SalesforceSDKManager.getInstance().hasNetwork()) {
                i(TAG, "onResumeLoggedInNotLoaded - remote start page/online - loading web app")
                loadRemoteStartPage(bootconfig.startPage, true)
            } else {

                // FIXME

//                // Has cached version
//                if (SalesforceWebViewClientHelper.hasCachedAppHome(this)) {
//                    i(
//                        TAG,
//                        "onResumeLoggedInNotLoaded - remote start page/offline/cached - loading cached web app"
//                    )
//                    loadCachedStartPage()
//                } else {
//                    i(
//                        TAG,
//                        "onResumeLoggedInNotLoaded - remote start page/offline/not cached - can not proceed"
//                    )
//                    loadErrorPage()
//                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onDestroy() {
        delegate.onDestroy()
        super.onDestroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return delegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event)
    }

    /**
     * Returns the unauthenticated start page from BootConfig.
     *
     * @return The unauthenticated start page
     */
    protected fun getUnauthenticatedStartPage(): String {
        return bootconfig.unauthenticatedStartPage
    }

    fun logout(/*callbackContext: CallbackContext?*/) {
        i(TAG, "logout called")
        SalesforceSDKManager.getInstance().logout(this)

        // FIXME

//        if (callbackContext != null) {
//            callbackContext.success()
//        }
    }

    /**
     * Get a RestClient and refresh the auth token
     *
     * @param callbackContext when not null credentials/errors are sent through to callbackContext.success()/error()
     */
    fun authenticate(/*callbackContext: CallbackContext?*/) {
        i(TAG, "authenticate called")
        clientManager.getRestClient(
            this
        ) { client ->
            if (client == null) {
                i(TAG, "authenticate callback triggered with null client")
                logout(/*null*/)
            } else {
                i(TAG, "authenticate callback triggered with actual client")
                this@SalesforceBridgeActivity.client = client

                /*
                 * Do a cheap REST call to refresh the access token if needed.
                 * If the login took place a while back (e.g. the already logged
                 * in application was restarted), then the returned session ID
                 * (access token) might be stale. This is not an issue if one
                 * uses exclusively RestClient for calling the server because
                 * it takes care of refreshing the access token when needed,
                 * but a stale session ID will cause the WebView to redirect
                 * to the web login.
                 */
                this@SalesforceBridgeActivity.client!!.sendAsync(
                    /*RestRequest.*/getRequestForLimits(
                        ApiVersionStrings.VERSION_NUMBER
                    ), object : AsyncRequestCallback {
                        override fun onSuccess(request: RestRequest, response: RestResponse) {
                            runOnUiThread { /*
                                                             * The client instance being used here needs to be
                                                             * refreshed, to ensure we use the new access token.
                                                             */
                                this@SalesforceBridgeActivity.client =
                                    this@SalesforceBridgeActivity.clientManager.peekRestClient()
                                getAuthCredentials(/*callbackContext*/)
                            }
                        }

                        override fun onError(exception: Exception) {
//                            if (callbackContext != null) {
//                                callbackContext.error(exception.message)
//                            }
                        }
                    })
            }
        }
    }

    /**
     * Get json for credentials
     *
     * @param callbackContext
     */
    fun getAuthCredentials(/*callbackContext: CallbackContext?*/) {
        i(TAG, "getAuthCredentials called")
        if (client != null) {
            val credentials = client!!.jsonCredentials
//            if (callbackContext != null) {
//                callbackContext.success(credentials)
//            }
        } else {
//            if (callbackContext != null) {
//                callbackContext.error("Never authenticated")
//            }
        }
    }

    /**
     * If an action causes a redirect to the login page, this method will be called.
     * It causes the session to be refreshed and reloads url through the front door.
     *
     * @param url the page to load once the session has been refreshed.
     */
    fun refresh(url: String?) {
        i(TAG, "refresh called")

        /*
         * If client is null at this point, authentication hasn't been performed yet.
         * We need to trigger authentication, and recreate the webview in the
         * callback, to load the page correctly. This handles some corner cases
         * involving hitting the back button when authentication is in progress.
         */if (client == null) {
            clientManager.getRestClient(
                this
            ) { recreate() }
            return
        }
        client!!.sendAsync(
            /*RestRequest.*/getRequestForLimits(ApiVersionStrings.VERSION_NUMBER),
            object : AsyncRequestCallback {
                override fun onSuccess(request: RestRequest, response: RestResponse) {
                    i(TAG, "refresh callback - refresh succeeded")
                    runOnUiThread { /*
                                 * The client instance being used here needs to be refreshed, to ensure we
                                 * use the new access token. However, if the refresh token was revoked
                                 * when the app was in the background, we need to catch that exception
                                 * and trigger a proper logout to reset the state of this class.
                                 */
                        try {
                            this@SalesforceBridgeActivity.client =
                                this@SalesforceBridgeActivity.clientManager.peekRestClient()
                            if (url != null) {
                                loadRemoteStartPage(url, true)
                            }
                        } catch (e: AccountInfoNotFoundException) {
                            i(TAG, "User has been logged out.")
                            logout(/*null*/)
                        }
                    }
                }

                override fun onError(exception: Exception) {
                    w(TAG, "refresh callback - refresh failed", exception)

                    // Only logout if we are NOT offline
                    if (exception !is NoNetworkException) {
                        logout(/*null*/)
                    }
                }
            })
    }

    /**
     * Load local start page
     */
    fun loadLocalStartPage() {
        i(TAG, "loadLocalStartPage called - loading!")
        assert(bootconfig.isLocal)
        val url = "${bridge.config.androidScheme}://${bridge.config.hostname}/${bootconfig.startPage}"
        bridge.webView.loadUrl(url)
        webAppLoaded = true
    }

    /**
     * Load the remote start page.
     * @param startPageUrl The start page to load.
     * @param loadThroughFrontDoor Whether or not to load through front-door.
     */
    private fun loadRemoteStartPage(startPageUrl: String, loadThroughFrontDoor: Boolean) {
        i(TAG, "loadRemoteStartPage called - loading!")
        assert(!bootconfig.isLocal)
        val url = if (loadThroughFrontDoor) getFrontDoorUrl(startPageUrl) else startPageUrl
        bridge.webView.loadUrl(url)
        webAppLoaded = true
    }

    /**
     * Returns the front-doored URL of a URL passed in.
     *
     * @param url      URL to be front-doored.
     * @return Front-doored URL.
     */
    fun getFrontDoorUrl(url: String?): String {

        /*
         * We need to use the absolute URL in some cases and relative URL in some
         * other cases, because of differences between instance URL and community
         * URL. Community URL can be custom and the logic of determining which
         * URL to use is in the 'resolveUrl' method in 'ClientInfo'.
         */
        val clientInfo = client!!.clientInfo
        val authToken = client!!.authToken
        val isAbsUrl = BootConfig.isAbsoluteUrl(url)
        val retURL = if (isAbsUrl) url else clientInfo.resolveUrl(url).toString()
        return clientInfo.resolveUrl("/secur/frontdoor.jsp").toString()
            .plus("?sid=${URLEncoder.encode(authToken, UTF_8)}")
            .plus("&retURL=${URLEncoder.encode(retURL, UTF_8)}")
            .plus("&display=touch")
    }

    /**
     * Load cached start page
     */
    private fun loadCachedStartPage() {
//        val url: String = SalesforceWebViewClientHelper.getAppHomeUrl(this)
//        loadUrl(url)
        webAppLoaded = true
    }

    /**
     * Load error page
     */
    fun loadErrorPage() {
        i(TAG, "loadErrorPage called")
        bridge.webView.loadUrl(bootconfig.errorPage)
    }

    override fun onLogoutComplete() {}

    override fun onUserSwitched() {
        if (client != null) {
            try {
                val currentClient = clientManager.peekRestClient()
                if (currentClient != null && currentClient.clientInfo.userId != client!!.clientInfo.userId) {
                    recreate()
                }
            } catch (e: AccountInfoNotFoundException) {
                i(TAG, "restartIfUserSwitched - no user account found")
            }
        }
    }
}