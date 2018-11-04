/*
 * ProtoBLE - Protobuf RPC over Bluetooth Low Energy
 * Copyright (c) 2018. Geoffrey Matrangola, electrazoom.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 *     This program is also available under a commercial license. If you wish
 *     to redistribute this library and derivative work for commercial purposes
 *     please see ProtoBLE.com to obtain a proprietary license that will fit
 *     your needs.
 */

package com.electrazoom.protoble.mobile

import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import com.electrazoom.protoble.example.Hello
import com.electrazoom.protoble.example.HelloWorldBleCentralService
import kotlinx.android.synthetic.main.activity_hello.*
import java.util.*

/**
 * The obligatory Hello World example. The Activity uses the HelloWorldClientService generated from
 * the Protobuf definitions in the ExampleHelloWorldApi using CodeGen.
 */
class HelloActivity : AppCompatActivity() {
    private val TAG = "HelloActivity"

    inner class HelloListener : HelloWorldBleCentralService.HelloWorldListener {
        override fun onProgress(uuid: UUID?, current: Int, total: Int) {
            Log.d(TAG, "Download Progress $current / $total")
        }

        override fun onConnectionStateChange(state: Int) {
            log("onConnectionStatusChange: $state")
            if (state == STATE_DISCONNECTED) {
                runOnUiThread {
                    sendButton.isEnabled = false
                    reconnectButton.isEnabled = true
                }
            }
            else log("GATT connected")
        }

        override fun onHelloWorld(output: Hello.Greeting?) {
            runOnUiThread {responseText.text =
                    "msg: ${output?.timesttamp} ${output?.greeting}"}
        }

        override fun onGetTime(output: Hello.TimeResponse?) {
            log("time: ${output?.formattedTime}")
        }

        override fun onServiceConnected() {
            runOnUiThread {
                sendButton.isEnabled = true
                reconnectButton.isEnabled = false
            }
        }

        override fun onError(error: String?) {
            log("Error: $error")
        }
    }
    private var clientService: HelloWorldBleCentralService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            log("onServiceConnected $name")
            val binder = service as HelloWorldBleCentralService.LocalBinder
            if (null != binder.service) {
                Log.d(TAG, "Serivce bound, calling listener")
                clientService = binder.service
                binder.service.setHelloWorldListener(HelloListener())
                connectToBleGatt();
            }
            else log("No service bound")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            log("onServiceDisconnected")
            clientService = null
            sendButton.isEnabled = false
        }
    }

    private fun connectToBleGatt() {
        val result = intent.getParcelableExtra<ScanResult>("scanResult")
        log("connectToBleGatt scanResult: $result")
        if (null != result) {
            val device = result.device
            clientService?.connect(device)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        sendButton.setOnClickListener({ _ -> sayHello() })
        reconnectButton.setOnClickListener( { _ -> connectToBleGatt()} )
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, HelloWorldBleCentralService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        if (null != clientService) unbindService(serviceConnection)
    }

    private fun sayHello() {
        Log.d(TAG, "sayHello")
        val builder = Hello.Introduction.newBuilder()
        builder.name = salutationText.text.toString()
        builder.salutation = nameText.text.toString()
        val message = builder.build()
        clientService!!.helloWorld(message)
    }

    private fun log(message: String) {
        Log.d(TAG, message)
        runOnUiThread {
            statusTextView.text = "${statusTextView.text}\n$message"
            statusScroll.post { statusScroll.fullScroll(View.FOCUS_DOWN) }
        }
    }
}
