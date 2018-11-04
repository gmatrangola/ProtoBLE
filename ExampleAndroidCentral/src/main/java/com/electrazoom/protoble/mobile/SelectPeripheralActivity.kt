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

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.electrazoom.protoble.example.EchoUtil
import kotlinx.android.synthetic.main.activity_select_peripheral.*
import com.electrazoom.protoble.example.HelloWorldConstants
import com.github.florent37.runtimepermission.kotlin.askPermission;

/**
 * Activity that lists the BLE Peripherals in range that are advertising the Echo or HelloWorld
 * service UUIDs.
 */
class SelectPeripheralActivity : AppCompatActivity() {
    private val TAG = "SelectPeripheral"

    private val REQUEST_ENABLE_BT = 1
    private var scanner: BluetoothLeScanner? = null
    private val found: MutableMap<String, ScanResult> = mutableMapOf()
    private var count = 0;

    private val helloServiceUuid = ParcelUuid.fromString(HelloWorldConstants.SERVICE_UUID)
    private val echoServiceUuid = ParcelUuid.fromString(EchoUtil.BULK_SERVICE_GUID)

    // Find instance of example custom peripheral service from https://github.com/tongo/ble-java
    private val filters = listOf(
            ScanFilter.Builder().setServiceUuid(helloServiceUuid).build(),
            ScanFilter.Builder().setServiceUuid(echoServiceUuid).build()
            )

    private val scanSettings = ScanSettings.Builder().
            setReportDelay(250).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_peripheral)
        Log.d(TAG, "onCreate")
        scanResultView.layoutManager = LinearLayoutManager(this)
        findAdapterWithPermission()
    }

    private fun findAdapterWithPermission() {
        askPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
            findAdapter()
        }.onDeclined { e ->
            Log.e(TAG, "Permission deined")
        }
    }

    private fun findAdapter(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if (adapter?.isEnabled == false) {
            Log.d(TAG, "Adapter not enabled.")
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
            return false
        }
        startScan(adapter)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            findAdapter()
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(TAG, "onBatchScanResults " + results?.size)
            if (results != null) runOnUiThread {
                addScanResults(results)
            }
        }
    }

    private fun addScanResults(results: Collection<ScanResult>) {
        val updates = results.associateBy( {it.device.address}, {it})
        for (entry in updates.entries) {
            found.put(entry.key, entry.value)
        }
        statusText.text = "Scan #${count++} adapters: ${results.size} total: ${found.size}"
        scanResultView.adapter = ScanResultAdapter(found.values.sortedBy { it.device.address }) {
            Log.d(TAG, "clicked on $it")
            scanner?.stopScan(scanCallback);
            onPariferialSelected(it)
        }
    }

    private fun onPariferialSelected(scanResult: ScanResult) {
        intent.putExtra("scanResult", scanResult)
        val uuids = scanResult.scanRecord.serviceUuids
        val intent : Intent?
        if (uuids.contains(helloServiceUuid)) {
            intent = Intent(this, HelloActivity::class.java)
        }
        else if (uuids.contains(echoServiceUuid)){
            intent = Intent(this, EchoActivity::class.java)
        }
        else intent = null
        if (intent != null) {
            intent.putExtra("scanResult", scanResult)
            startActivity(intent)
        }
    }

    fun startScan(adapter: BluetoothAdapter) {
        scanner = adapter.bluetoothLeScanner
        if (null != scanner) {
            scanner?.startScan(filters, scanSettings, scanCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestory");
        if (null != scanner) {
            scanner?.stopScan(scanCallback);
        }
    }
}
