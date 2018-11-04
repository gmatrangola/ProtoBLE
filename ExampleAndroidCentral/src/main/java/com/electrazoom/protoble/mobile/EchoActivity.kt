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

import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kotlinx.android.synthetic.main.activity_echo.*

/**
 * Android UI Activity to enable testing of messages of arbitrary sizes over the ProtoBLE library
 * These tests are do not include Protobuf RPC, or encoding, they are just low-level byte streams.
 */
class EchoActivity : AppCompatActivity() {
    private val TAG = "Echo"

    inner class EchoListener : EchoBleCentralService.EchoListener {
        override fun onEcho(len: Int, message: String?) {
            Log.d(TAG, "onEcho ${len}: ${message}")
            runOnUiThread {
                receivedCountText.text = "${len}"
                receivedText.text = message
            }
        }
    }
    private var clientService: EchoBleCentralService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected $name")
            val binder = service as EchoBleCentralService.LocalBinder
            if (null != binder.service) {
                Log.d(TAG, "Serivce bound, calling listener")
                binder.service.setListener(EchoListener())
                clientService = binder.service
                connectToBleGatt();
            }
            else Log.e(TAG, "No service bound")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            clientService = null
            startButton.isEnabled = false
            readButton.isEnabled = false
        }
    }

    private fun connectToBleGatt() {
        val result = intent.getParcelableExtra<ScanResult>("scanResult")
        Log.d(TAG, "Selected Scan result = " + result)
        if (null != result) {
            val device = result.device
            clientService?.connect(device)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_echo)
        startButton.setOnClickListener { _ -> startTest() }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        val intent = Intent(this, EchoBleCentralService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        if (null != clientService) unbindService(serviceConnection)
    }

    private fun startTest() {
        Log.d(TAG, "startTest")
        clientService!!.writeMessage("Hello World!!")
        for (i in 17..21) {
            var bytes = ByteArray(i)
            for (j in 0..i-1) {
                bytes[j] = j.toByte()
            }
            writeMessage(bytes)
            statusText.text = "Sent ${i}"
        }
    }

    private fun writeMessage(bytes: ByteArray) {
        val content = bytes.contentToString()
        Log.d(TAG, "writeMessage ${bytes.size}: $content")
        sentCountText.text = "${bytes.size}"
        sentText.text = content
        clientService!!.writeMessage(bytes)
    }
}
