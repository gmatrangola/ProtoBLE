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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.scan_result_layout.view.*


/**
 * Show one of the ScanResult object returned from a scan
 */
class ScanResultAdapter(val scanResults: List<ScanResult>,
                        private val listener: (ScanResult) -> Unit) :
        RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.scan_result_layout, parent, false);
        return ViewHolder(layout)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(scanResults[position], listener)

    override fun getItemCount(): Int = scanResults.size

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(results: ScanResult, viewListener: (ScanResult) -> Unit) = with(view) {
            val name = if(results.device.name == null)"---" else results.device.name
            val status: String
            if(android.os.Build.VERSION.SDK_INT >= 26) {
                val connectable = if(results.isConnectable) "Connectable" else "no"
                status = "timestamp: ${results.timestampNanos}ns tx: ${results.txPower} dB " +
                        "rssi: ${results.rssi} dB " +
                        "interval: ${results.periodicAdvertisingInterval} ${connectable}"
            }
            else {
                status = "timestamp: ${results.timestampNanos}ns rssi: ${results.rssi} dB"
            }
            view.remoteDeviceText.text = "${results.device.address} ${name}"
            view.detailsText.text = "${status}"
            setOnClickListener { listener (results)}
        }
    }
}

