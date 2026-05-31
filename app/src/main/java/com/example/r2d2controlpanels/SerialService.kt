package com.example.r2d2controlpanels.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.example.r2d2controlpanels.SerialListener
import com.example.r2d2controlpanels.SerialSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.ArrayDeque

class SerialService : SerialListener {

    private val scope =
        CoroutineScope(
            Dispatchers.IO +
                    SupervisorJob()
        )

    private var socket: SerialSocket? = null

    private var listener: SerialListener? = null

    var isConnected = false
        private set

    // ==================================================
    // CONNECT
    // ==================================================

    fun connectBluetooth(
        context: Context,
        deviceAddress: String,
        onStatus: (String) -> Unit
    ) {
        disconnect()
        try {

            val bluetoothManager =

                context.getSystemService(
                    Context.BLUETOOTH_SERVICE
                ) as BluetoothManager

            val bluetoothAdapter =
                bluetoothManager.adapter

            val device: BluetoothDevice =

                bluetoothAdapter
                    .getRemoteDevice(
                        deviceAddress
                    )

            val socket =

                SerialSocket(
                    context.applicationContext,
                    device
                )

            this.socket = socket

            socket.connectToBluetooth(

                listener = this,

                onConnected = {

                    isConnected = true

                    onStatus("CONNECTED")

                    Log.d(
                        "BT",
                        "CONNECTED"
                    )
                },

                onError = { e ->

                    isConnected = false

                    Log.e(
                        "BT",
                        "CONNECT FAILED",
                        e
                    )

                    onStatus(
                        "FAILED: ${e.message}"
                    )
                }
            )

        } catch (e: Exception) {

            isConnected = false

            Log.e(
                "BT",
                "CONNECT FAILED",
                e
            )

            onStatus(
                "FAILED: ${e.message}"
            )
        }
    }

    // ==================================================
    // WRITE
    // ==================================================

    @Throws(IOException::class)
    fun writeToBluetooth(data: ByteArray) {

        if (!isConnected) {

            throw IOException(
                "Bluetooth not connected"
            )
        }

        socket?.writeToBluetooth(data)
    }

    // ==================================================
    // DISCONNECT
    // ==================================================

    fun disconnect() {

        isConnected = false

        try {

            socket?.disconnectBluetooth()

        } catch (e: Exception) {

            Log.e(
                "BT",
                "DISCONNECT FAILED",
                e
            )
        }

        socket = null
    }

    // ==================================================
    // LISTENER
    // ==================================================

    fun attach(listener: SerialListener) {

        this.listener = listener
    }

    fun detach() {

        listener = null
    }

    // ==================================================
    // CALLBACKS
    // ==================================================

    override fun onSerialConnect() {

        scope.launch {

            listener?.onSerialConnect()
        }
    }

    override fun onSerialConnectError(
        e: Exception?
    ) {

        isConnected = false

        scope.launch {

            listener?.onSerialConnectError(e)
        }
    }

    override fun onSerialRead(
        data: ByteArray?
    ) {

        scope.launch {

            listener?.onSerialRead(data)
        }
    }

    override fun onSerialRead(
        datas: ArrayDeque<ByteArray?>?
    ) {
    }

    override fun onSerialIoError(
        e: Exception?
    ) {

        isConnected = false

        scope.launch {

            listener?.onSerialIoError(e)
        }
    }
}