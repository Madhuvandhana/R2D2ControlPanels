package com.example.r2d2controlpanels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.r2d2controlpanels.data.SerialService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothController @Inject constructor(

    private val serialService: SerialService

) {

    companion object {

        private const val TAG =
            "BluetoothController"
    }

    // ==================================================
    // CONNECTION STATE
    // ==================================================

    var connectionState by mutableStateOf(
        BluetoothState.DISCONNECTED
    )
        private set

    val isConnected: Boolean
        get() =
            connectionState ==
                    BluetoothState.CONNECTED

    // ==================================================
    // CONNECT
    // ==================================================

    fun connect(
        context: Context,
        address: String
    ) {
        // ==================================================
        // PREVENT DUPLICATE CONNECTIONS
        // ==================================================

        if (

            connectionState ==
            BluetoothState.CONNECTED

        ) {

            Log.d(
                TAG,
                "ALREADY CONNECTED"
            )

            return
        }

        if (

            connectionState ==
            BluetoothState.CONNECTING

        ) {

            Log.d(
                TAG,
                "ALREADY CONNECTING"
            )

            return
        }

        // ==================================================
        // START CONNECT
        // ==================================================

        connectionState =
            BluetoothState.CONNECTING

        serialService.connectBluetooth(

            context = context,

            deviceAddress = address

        ) { status ->

            Log.d(
                TAG,
                status
            )

            when {

                status == "CONNECTED" -> {

                    connectionState =
                        BluetoothState.CONNECTED
                }

                status.startsWith("FAILED") -> {

                    connectionState =
                        BluetoothState.DISCONNECTED
                }
            }
        }
    }

    // ==================================================
    // SEND
    // ==================================================

    fun send(command: String) {

        if (!isConnected) {

            Log.d(
                TAG,
                "NOT CONNECTED"
            )

            return
        }

        try {

            serialService.writeToBluetooth(
                command.toByteArray()
            )

            Log.d(
                TAG,
                "SENT: $command"
            )

        } catch (e: Exception) {

            connectionState =
                BluetoothState.DISCONNECTED

            Log.e(
                TAG,
                "SEND FAILED",
                e
            )
        }
    }

    // ==================================================
    // DISCONNECT
    // ==================================================

    fun disconnect() {

        try {

            serialService.disconnect()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "DISCONNECT FAILED",
                e
            )
        }

        connectionState =
            BluetoothState.DISCONNECTED
    }
}