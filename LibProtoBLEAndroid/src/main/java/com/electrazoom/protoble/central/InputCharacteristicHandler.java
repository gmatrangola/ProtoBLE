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

package com.electrazoom.protoble.central;

import android.util.Log;

import com.electrazoom.protoble.valuemessage.OutputMessageQueue;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/**
 * Writes messages to the InputCharacteristic on the BLE Peripheral. This looks like a parameter
 * in the Protobuf definition.
 */
public class InputCharacteristicHandler extends CharacteristicHandler {
    private static final String TAG = "InputCharHandler";
    private int writeMessageCount = 0;
    private Thread writeThread = null;
    private OutputMessageQueue outputMessageQueue = new OutputMessageQueue();

    public InputCharacteristicHandler(ProtoBleCentralService protoBleCentralService,
                                      UUID uuid,
                                      ProtoBleCentralService.ResponseListener listener) {
        super(protoBleCentralService, uuid, listener);
    }

    @Override
    public void onConnect() {
        Log.d(TAG, "onConnect");
        outputMessageQueue.clear();
        writeMessageCount = 0;
        startWriteProcessor();
    }

    public void startWriteProcessor() {
        if (writeThread == null) {
            Log.d(TAG,  "starting new write processor");
            writeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writeProcessor();
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Fail in writeProcessor", e);
                    }
                }
            });
            writeThread.start();
        }
    }

    public void writeMessage(byte[] message) {
        writeMessageCount++;
        Log.d(TAG, "writeMessage " + writeMessageCount + " : " + message.length + ": " +
                new String(Hex.encodeHex(message)));
        outputMessageQueue.add(message);
    }

    private void writeProcessor() {
        while(service.isRunning()) {
            try {
                Thread.sleep(500);
                if (getCharacteristic() != null) {
                    byte[] value = outputMessageQueue.waitForNextValue();
                    if (value != null) writeBuffer(value);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "unable to get next buffer", e);
            }
        }
    }

    private synchronized void writeBuffer(byte[] buffer) {
        Log.d(TAG, "writeNextBuffer " + writeMessageCount + " " + buffer.length + ": " +
                new String(Hex.encodeHex(buffer)));
        getCharacteristic().setValue(buffer);
        // send to Input Characteristic
        if (!service.getGattConnection().writeCharacteristic(getCharacteristic())) {
            Log.e(TAG, "Unable to write input characteristic buffer " + buffer.length + ": " +
                    new String(Hex.encodeHex(buffer)));
            listener.onError("Unable to write input characteristic buffer");
            writeMessageCount = 0;
            outputMessageQueue.clear();
        }
        else Log.d(TAG, "writing buffer " + writeMessageCount);
    }

}
