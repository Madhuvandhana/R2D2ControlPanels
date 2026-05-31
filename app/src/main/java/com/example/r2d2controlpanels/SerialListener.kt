package com.example.r2d2controlpanels

import java.util.ArrayDeque

interface SerialListener {

    fun onSerialConnect()

    fun onSerialConnectError(
        e: Exception?
    )

    fun onSerialRead(
        data: ByteArray?
    )

    fun onSerialRead(
        datas: ArrayDeque<ByteArray?>?
    )

    fun onSerialIoError(
        e: Exception?
    )
}