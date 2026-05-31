package com.example.r2d2controlpanels

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.security.InvalidParameterException
import java.util.UUID

private const val TAG = "SerialSocket"

class SerialSocket(

    private val context: Context,

    private val bluetoothDevice: BluetoothDevice

) {

    private var listener: SerialListener? = null

    private var bluetoothSocket: BluetoothSocket? = null

    private var connected = false

    private val job = Job()

    private val scope =
        CoroutineScope(
            Dispatchers.IO + job
        )

    companion object {

        val BLUETOOTH_SPP_UUID: UUID =

            UUID.fromString(
                "00001101-0000-1000-8000-00805F9B34FB"
            )
    }

    init {

        if (context is Activity) {

            throw InvalidParameterException(
                "Use applicationContext"
            )
        }
    }

    // ==================================================
    // CONNECT
    // ==================================================

    fun connectToBluetooth(

        listener: SerialListener,

        onConnected: () -> Unit,

        onError: (Exception) -> Unit

    ) {

        this.listener = listener

        scope.launch {

            connectInternal(
                onConnected,
                onError
            )
        }
    }

    // ==================================================
    // INTERNAL CONNECT
    // ==================================================

    @SuppressLint("MissingPermission")
    private fun connectInternal(

        onConnected: () -> Unit,

        onError: (Exception) -> Unit

    ) {

        try {

            bluetoothSocket =

                bluetoothDevice
                    .createRfcommSocketToServiceRecord(
                        BLUETOOTH_SPP_UUID
                    )

            bluetoothSocket?.connect()

            connected = true

            Log.d(
                TAG,
                "BLUETOOTH CONNECTED"
            )

            onConnected.invoke()

            listener?.onSerialConnect()

            readLoop()

        } catch (e: Exception) {

            connected = false

            Log.e(
                TAG,
                "CONNECT FAILED",
                e
            )

            onError.invoke(e)

            listener?.onSerialConnectError(e)

            disconnectBluetooth()
        }
    }

    // ==================================================
    // READ LOOP
    // ==================================================

    private fun readLoop() {

        try {

            val buffer = ByteArray(1024)

            while (
                connected &&
                bluetoothSocket != null
            ) {

                val inputStream =
                    bluetoothSocket?.inputStream
                        ?: break

                val len =
                    inputStream.read(buffer)

                // SOCKET CLOSED

                if (len <= 0) {

                    Log.d(
                        TAG,
                        "SOCKET CLOSED"
                    )

                    break
                }

                val data =
                    buffer.copyOf(len)

                listener?.onSerialRead(data)
            }

        } catch (e: IOException) {

            Log.e(
                TAG,
                "READ LOOP CLOSED",
                e
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "READ LOOP ERROR",
                e
            )

        } finally {

            connected = false

            listener?.onSerialIoError(
                IOException(
                    "Bluetooth disconnected"
                )
            )

            disconnectBluetooth()
        }
    }

    // ==================================================
    // WRITE
    // ==================================================

    @Throws(IOException::class)
    fun writeToBluetooth(data: ByteArray) {

        if (!connected) {

            throw IOException(
                "Bluetooth not connected"
            )
        }

        bluetoothSocket
            ?.outputStream
            ?.write(data)

        bluetoothSocket
            ?.outputStream
            ?.flush()
    }

    // ==================================================
    // DISCONNECT
    // ==================================================

    fun disconnectBluetooth() {

        connected = false

        listener = null

        try {

            bluetoothSocket
                ?.inputStream
                ?.close()

        } catch (_: Exception) {
        }

        try {

            bluetoothSocket
                ?.outputStream
                ?.close()

        } catch (_: Exception) {
        }

        try {

            bluetoothSocket?.close()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "DISCONNECT ERROR",
                e
            )
        }

        bluetoothSocket = null

        // VERY IMPORTANT

        job.cancel()

        Log.d(
            TAG,
            "BLUETOOTH DISCONNECTED"
        )
    }

    // ==================================================
    // STATUS
    // ==================================================

    fun isConnected(): Boolean {

        return connected
    }

    fun getBluetoothSocket(): BluetoothSocket? {

        return bluetoothSocket
    }
}