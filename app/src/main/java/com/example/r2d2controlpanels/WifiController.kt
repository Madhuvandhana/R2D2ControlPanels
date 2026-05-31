package com.example.r2d2controlpanels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class WifiController {

    companion object {

        private const val TAG =
            "WifiController"

        private const val ASTRO_URL =
            "http://192.168.4.1"
    }

    // ==================================================
    // WIFI STATUS
    // ==================================================

    var connectionState by mutableStateOf(
        WifiState.CONNECTING
    )

    val isConnected: Boolean
        get() =
            connectionState ==
                    WifiState.CONNECTED

    // ==================================================
    // SEND COMMAND
    // ==================================================

    fun send(
        command: String
    ) {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                connectionState =
                    WifiState.CONNECTING

                val encoded =

                    java.net.URLEncoder.encode(

                        command,

                        "UTF-8"
                    )

                val url =

                    URL(
                        "$ASTRO_URL/marcduino?cmd=$encoded"
                    )

                Log.d(
                    TAG,
                    "URL: $url"
                )

                val connection =

                    url.openConnection()
                            as HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    3000

                connection.readTimeout =
                    3000

                connection.connect()

                val responseCode =
                    connection.responseCode

                Log.d(
                    TAG,
                    "HTTP: $responseCode"
                )

                connectionState =

                    if (responseCode in 200..299)
                        WifiState.CONNECTED
                    else
                        WifiState.DISCONNECTED

                val response =

                    connection.inputStream
                        ?.bufferedReader()
                        ?.readText()

                Log.d(
                    TAG,
                    "RESPONSE: $response"
                )

                connection.disconnect()

            } catch (e: Exception) {

                connectionState =
                    WifiState.DISCONNECTED

                Log.e(
                    TAG,
                    "WIFI SEND FAILED",
                    e
                )
            }
        }
    }
    // ==================================================
    // PING / TEST CONNECTION
    // ==================================================

    fun testConnection() {

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                connectionState =
                    WifiState.CONNECTING

                val url =
                    URL(ASTRO_URL)

                val connection =

                    url.openConnection()
                            as HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    3000

                connection.readTimeout =
                    3000

                connection.connect()

                val code =
                    connection.responseCode

                connectionState =

                    if (code in 200..299)
                        WifiState.CONNECTED
                    else
                        WifiState.DISCONNECTED

                connection.disconnect()

            } catch (e: Exception) {

                connectionState =
                    WifiState.DISCONNECTED

                Log.e(
                    TAG,
                    "WIFI TEST FAILED",
                    e
                )
            }
        }
    }
}