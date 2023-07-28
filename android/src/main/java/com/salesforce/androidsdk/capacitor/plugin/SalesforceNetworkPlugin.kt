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

import android.text.TextUtils
import android.util.Base64
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.PluginResult
import com.getcapacitor.annotation.CapacitorPlugin
import com.salesforce.androidsdk.capacitor.ui.SalesforceBridgeActivity
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger.e
import com.salesforce.androidsdk.capacitor.util.SalesforceHybridLogger.i
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback
import com.salesforce.androidsdk.rest.RestRequest
import com.salesforce.androidsdk.rest.RestRequest.RestMethod
import com.salesforce.androidsdk.rest.RestResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder


@CapacitorPlugin(name = "SalesforceNetworkPlugin")
class SalesforceNetworkPlugin : Plugin() {

    @PluginMethod
    fun sendRequest(call: PluginCall) {
        i(TAG, "sendRequest called")
        try {
            val request = prepareRestRequest(call)
            val returnBinary = call.getBoolean(RETURN_BINARY, false)!!
            val doesNotRequireAuth = call.getBoolean(DOES_NOT_REQUIRE_AUTHENTICATION, false)!!

            // Sends the request.
            val restClient = getRestClient(doesNotRequireAuth) ?: return
            restClient.sendAsync(request, object : AsyncRequestCallback {
                override fun onSuccess(request: RestRequest, response: RestResponse) {
                    try {
                        // Not a 2xx status
                        if (!response.isSuccess) {
                            val responseObject = JSONObject()
                            // FIXME responseObject.put("headers", JSONObject(response.allHeaders))
                            responseObject.put("statusCode", response.statusCode)
                            responseObject.put("body", parsedResponse(response))
                            val errorObject = JSONObject()
                            errorObject.put("response", responseObject)
                            call.errorCallback(errorObject.toString())
                        } else if (returnBinary) {
                            val result = PluginResult()
                            result.put(CONTENT_TYPE, response.contentType)
                            result.put(
                                ENCODED_BODY,
                                Base64.encodeToString(response.asBytes(), Base64.DEFAULT)
                            )
                            call.successCallback(result)
                        } else if (response.asBytes().size > 0) {
                            val result = PluginResult()
//                            result.put("headers", response.allHeaders)
                            result.put("body", response.asString())
                            call.successCallback(result)
                        } else {
                            call.resolve()
                        }
                    } catch (e: Exception) {
                        e(TAG, "Error while parsing response", e)
                        onError(e)
                    }
                }

                override fun onError(exception: Exception) {
                    val errorObject = JSONObject()
                    try {
                        errorObject.put("error", exception.message)
                    } catch (jsonException: JSONException) {
                        e(TAG, "Error creating error object", jsonException)
                    }
                    call.errorCallback(errorObject.toString())
                }
            })
        } catch (exception: Exception) {
            val errorObject = JSONObject()
            try {
                errorObject.put("error", exception.message)
            } catch (jsonException: JSONException) {
                e(TAG, "Error creating error object", jsonException)
            }
            call.errorCallback(errorObject.toString())
        }
    }

    @Throws(IOException::class)
    private fun parsedResponse(response: RestResponse): Any {
        // Is it a JSONObject?
        val responseAsJSONObject = parseResponseAsJSONObject(response)
        if (responseAsJSONObject != null) {
            return responseAsJSONObject
        }

        // Is it a JSONArray?
        val responseAsJSONArray = parseResponseAsJSONArray(response)
        return responseAsJSONArray ?: response.asString()

        // Otherwise return as string
    }

    @Throws(IOException::class)
    private fun parseResponseAsJSONObject(response: RestResponse): JSONObject? {
        return try {
            response.asJSONObject()
        } catch (e: JSONException) {
            // Not a JSON object
            null
        }
    }

    @Throws(IOException::class)
    private fun parseResponseAsJSONArray(response: RestResponse): JSONArray? {
        return try {
            response.asJSONArray()
        } catch (e: JSONException) {
            // Not a JSON array
            null
        }
    }

    @Throws(
        UnsupportedEncodingException::class,
        URISyntaxException::class,
        JSONException::class
    )
    private fun prepareRestRequest(call: PluginCall): RestRequest? {
        val method = RestMethod.valueOf(call.getString(METHOD_KEY, RestMethod.GET.name)!!)
        val endPoint = call.getString(END_POINT_KEY)!!
        val path = call.getString(PATH_KEY)!!
        val queryParamString = call.getString(QUERY_PARAMS_KEY)
        var queryParams = JSONObject()
        if (!TextUtils.isEmpty(queryParamString)) {
            queryParams = JSONObject(queryParamString)
        }
// FIXME
//        val headerParams = call.getObject(HEADER_PARAMS_KEY)
//        val headerKeys = headerParams.keys()
        val additionalHeaders: MutableMap<String, String> = HashMap()
//        if (headerKeys != null) {
//            while (headerKeys.hasNext()) {
//                val headerKeyStr = headerKeys.next()
//                if (!TextUtils.isEmpty(headerKeyStr)) {
//                    additionalHeaders[headerKeyStr] = headerParams.optString(headerKeyStr)
//                }
//            }
//        }
        val fileParams = call.getObject(FILE_PARAMS_KEY)

        // Prepares the request.
        var urlParams = ""
        var requestBody: RequestBody? = null
        if (method == RestMethod.DELETE || method == RestMethod.GET || method == RestMethod.HEAD) {
            urlParams = buildQueryString(queryParams)
        } else {
            requestBody = buildRequestBody(queryParams, fileParams)
        }
        val separator =
            if (urlParams.isEmpty()) "" else if (path.contains("?")) (if (path.endsWith("&")) "" else "&") else "?"
        return RestRequest(
            method, endPoint + path + separator + urlParams,
            requestBody,
            additionalHeaders
        )
    }

    private fun getRestClient(doesNotRequireAuth: Boolean): RestClient? {
        val currentActivity: SalesforceBridgeActivity = (activity as? SalesforceBridgeActivity) ?: return null
        return if (doesNotRequireAuth) {
            currentActivity.buildClientManager().peekUnauthenticatedRestClient()
        } else currentActivity.client
    }

    companion object {
        private const val TAG = "SalesforceNetworkPlugin"
        private const val METHOD_KEY = "method"
        private const val END_POINT_KEY = "endPoint"
        private const val PATH_KEY = "path"
        private const val QUERY_PARAMS_KEY = "queryParams"
        private const val HEADER_PARAMS_KEY = "headerParams"
        private const val FILE_PARAMS_KEY = "fileParams"
        private const val FILE_MIME_TYPE_KEY = "fileMimeType"
        private const val FILE_URL_KEY = "fileUrl"
        private const val FILE_NAME_KEY = "fileName"
        private const val RETURN_BINARY = "returnBinary"
        private const val ENCODED_BODY = "encodedBody"
        private const val CONTENT_TYPE = "contentType"
        private const val DOES_NOT_REQUIRE_AUTHENTICATION = "doesNotRequireAuthentication"
        @Throws(UnsupportedEncodingException::class)
        private fun buildQueryString(params: JSONObject?): String {
            if (params == null || params.length() == 0) {
                return ""
            }
            val sb = StringBuilder()
            val keys = params.keys()
            if (keys != null) {
                while (keys.hasNext()) {
                    val keyStr = keys.next()
                    if (!TextUtils.isEmpty(keyStr)) {
                        sb.append(keyStr).append("=").append(
                            URLEncoder.encode(
                                params.optString(keyStr),
                                RestRequest.UTF_8
                            )
                        ).append("&")
                    }
                }
            }
            return sb.toString()
        }

        @Throws(URISyntaxException::class)
        private fun buildRequestBody(params: JSONObject, fileParams: JSONObject?): RequestBody {
            return if (fileParams == null || fileParams.length() == 0) {
                RequestBody.create(RestRequest.MEDIA_TYPE_JSON, params.toString())
            } else {
                val builder: Builder = Builder().setType(MultipartBody.FORM)
                val keys = params.keys()
                if (keys != null) {
                    while (keys.hasNext()) {
                        val keyStr = keys.next()
                        if (!TextUtils.isEmpty(keyStr)) {
                            builder.addFormDataPart(keyStr, params.optString(keyStr))
                        }
                    }
                }

                /*
                     * File params expected to be of the form:
                     * {<fileParamNameInPost>: {fileMimeType:<someMimeType>, fileUrl:<fileUrl>, fileName:<fileNameForPost>}}.
                     */
                val fileKeys = fileParams.keys()
                if (fileKeys != null) {
                    while (fileKeys.hasNext()) {
                        val fileKeyStr = fileKeys.next()
                        if (!TextUtils.isEmpty(fileKeyStr)) {
                            val fileParam = fileParams.optJSONObject(fileKeyStr)
                            if (fileParam != null) {
                                val mimeType = fileParam.optString(FILE_MIME_TYPE_KEY)
                                val name = fileParam.optString(FILE_NAME_KEY)
                                val url = URI(fileParam.optString(FILE_URL_KEY))
                                val file = File(url)
                                val mediaType: MediaType? = mimeType.toMediaTypeOrNull()
                                builder.addFormDataPart(
                                    fileKeyStr,
                                    name,
                                    file.asRequestBody(mediaType)
                                )
                            }
                        }
                    }
                }
                builder.build()
            }
        }
    }
}