package com.example.r2d2controlpanels

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothController: BluetoothController

    // ==================================================
    // YOUR ESP32 MAC ADDRESS
    // ==================================================

    private val esp32Address =

        "B0:A7:32:14:D5:4A"

    // Replace with YOUR real MAC address

    // ==================================================
    // START
    // ==================================================

    private val bluetoothPermissionLauncher =

        registerForActivityResult(

            ActivityResultContracts
                .RequestMultiplePermissions()

        ) { permissions ->

            val granted =

                permissions.entries.all {

                    it.value
                }

            if (granted) {

                connectBluetooth()

            } else {

                Log.e(
                    "BT",
                    "BLUETOOTH PERMISSION DENIED"
                )
            }
        }

    // ==================================================
    // CONNECT
    // ==================================================

    private fun connectBluetooth() {

        bluetoothController.connect(

            context = applicationContext,

            address = esp32Address
        )

        Log.d(
            "BT",
            "CONNECTING..."
        )
    }

    // ==================================================
    // PERMISSIONS
    // ==================================================

    private fun requestBluetoothPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val connectGranted =

                ContextCompat.checkSelfPermission(

                    this,

                    Manifest.permission.BLUETOOTH_CONNECT

                ) == PackageManager.PERMISSION_GRANTED

            val scanGranted =

                ContextCompat.checkSelfPermission(

                    this,

                    Manifest.permission.BLUETOOTH_SCAN

                ) == PackageManager.PERMISSION_GRANTED

            if (!connectGranted || !scanGranted) {

                bluetoothPermissionLauncher.launch(

                    arrayOf(

                        Manifest.permission.BLUETOOTH_CONNECT,

                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )

            } else {

                connectBluetooth()
            }

        } else {

            connectBluetooth()
        }
    }


    // ==================================================
    // STOP
    // ==================================================

    override fun onDestroy() {

        super.onDestroy()

        bluetoothController.disconnect()

        Log.d(
            "BT",
            "DISCONNECTED"
        )
    }

    // ==================================================
    // CREATE
    // ==================================================

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        requestBluetoothPermissions()

//        bluetoothController.connect(
//            context = this,
//            address = esp32Address
//        )
//
//        Log.d(
//            "BT",
//            "CONNECTING..."
//        )

        setContent {

            Scaffold(

                topBar = {

                    TopAppBar(

                        title = {

                            Text(
                                "R2 Control"
                            )
                        }
                    )
                }

            ) { innerPadding ->

                Surface(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                ) {

                    R2Screen(

                        bluetoothController =
                            bluetoothController
                    )
                }
            }
        }
    }
}