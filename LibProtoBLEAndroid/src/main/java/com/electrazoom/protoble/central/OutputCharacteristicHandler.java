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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.ParcelUuid;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.UUID;

import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static com.electrazoom.protoble.valuemessage.OutputMessageQueue.FIRST_BUFFSIZE;

/**
 * Reads the Return value from the BLE Peripheral. This registers with the Peripheral to get
 * notified of any changes so that it can go and read the value.
 */
public class OutputCharacteristicHandler extends CharacteristicHandler {
    // Standard Notification Descriptor
    public static final UUID CCCD_UUID = ParcelUuid.fromString("00002902-0000-1000-8000-00805f9b34fb").getUuid();
    private static final String TAG = "OutCharHandler";
    private BluetoothGattDescriptor cccd;
    private short currentMessageLen = 0;
    private int currentBytesRead = 0;
    private ByteBuffer currentBuffer;
    private int readMessageCount = 0;

    public OutputCharacteristicHandler(ProtoBleCentralService protoBleCentralService, UUID uuid,
                                       ProtoBleCentralService.ResponseListener listener) {
        super(protoBleCentralService, uuid, listener);
    }

    @Override
    public void onConnect() {
        Log.d(TAG, "onConnect");
        currentBuffer = null;
        currentMessageLen = 0;
        currentBytesRead = 0;
        readMessageCount = 0;
    }

    public void registerCccd(BluetoothGatt gatt) throws CentralServiceException {
        gatt.setCharacteristicNotification(getCharacteristic(), true);

        cccd = getCharacteristic().getDescriptor(CCCD_UUID);
        cccd.setValue(ENABLE_NOTIFICATION_VALUE);
        Log.d(TAG, "registerCccd " + getCharacteristic().getUuid() + " cccd.getPermissions: " + cccd.getPermissions());
        int retry = 10;
        while (!gatt.writeDescriptor(cccd) && retry > 0) {
            Log.e(TAG, "Unable to request notifications with the CCCD " + getUuid() + " " + retry + " more attempts.");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {

            }
            retry--;
        }
        if (retry == 0) throw new CentralServiceException("Unable to request notifications with the CCCD " + getUuid());
    }

    public void handleResponse() {
        byte[] value = getCharacteristic().getValue();
        if (value == null) {
            Log.e(TAG, "handleResponse value == null");
            listener.onError("Null Response");
            currentBuffer = null;
            currentBytesRead = 0;
            currentMessageLen = 0;
            return;
        }
        Log.d(TAG, "handleResponse " + value.length + " currentMessageLen = " + currentBytesRead + "/" + currentMessageLen + " currentBuffer " + currentBuffer);
        Log.d(TAG, "    msg= " + new String(Hex.encodeHex(value)));
        if (currentMessageLen == 0 || value.length == 0) {
            if ( value.length == 0 ) {
                notifyMessage(currentBuffer.array());
                currentBuffer = null;
                currentBytesRead = 0;
                currentMessageLen = 0;
                readMessageCount = 0;
            }
            else {
                if (!readFirstBuffer(value)) {
                    readNextMessage();
                }
            }
        }
        else {
            currentBuffer.put(value);
            currentBytesRead += value.length;
            if (currentBytesRead < currentMessageLen) {
                readNextMessage();
            }
            else {
                notifyMessage(currentBuffer.array());
                currentBuffer = null;
                currentBytesRead = 0;
                currentMessageLen = 0;
                readMessageCount = 0;
            }
        }
        service.notifyProgress(uuid, currentBytesRead, currentMessageLen);
    }

    /**
     * Read the part of the message in the value
     * @param value byte[]
     * @return true if we have the whole message, false if we need to read more
     */
    private boolean readFirstBuffer(byte[] value) {
        boolean isEom;
        ByteBuffer firstBuffer = ByteBuffer.wrap(value);
        currentMessageLen = firstBuffer.getShort();
        Log.d(TAG, "readFirstBuffer msgLen = " + currentMessageLen);
        if (currentMessageLen <= FIRST_BUFFSIZE) {
            byte[] array = new byte[currentMessageLen];
            firstBuffer.get(array);
            Log.d(TAG, "   readFirstMessage -> notifyMessage: " + array.length + ": " + new String(Hex.encodeHex(array)) );
            notifyMessage(array);
            currentMessageLen = 0;
            isEom = true;
        }
        else {
            byte[] array = new byte[FIRST_BUFFSIZE];
            firstBuffer.get(array);
            currentBuffer = ByteBuffer.allocate(currentMessageLen);
            currentBytesRead = array.length;
            Log.d(TAG, "   readFirstMessage -> currentBuffer.put: " + array.length + ": " + new String(Hex.encodeHex(array)) );
            currentBuffer.put(array);
            isEom = false;
        }
        return isEom;
    }

    public void readNextMessage() {
        // readCharacteristic has to be called from another thread if it is being called after
        // an on* call
        readMessageCount++;
        final int count = readMessageCount;
        Log.d(TAG, "readNextMessage " + count);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int maxTries = 5;
                while (service.getGattConnection() != null &&
                        !service.getGattConnection().readCharacteristic(getCharacteristic()) &&
                        maxTries > 0) {
                    Log.w(TAG, "readNextMessage: Unable to read service output characteristic " + maxTries);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    maxTries--;
                }
                if (maxTries == 0) Log.e(TAG, "Unable to readNextMessage()");
                else Log.d(TAG, "readNextMessageThread count = " + count + " readMessageCount = " + readMessageCount);
            }
        }).start();
    }

    private void notifyMessage(byte[] array) {
        Log.d(TAG, "notifyMessage " + new String(Hex.encodeHex(array)));
        if(listener != null) listener.onResponse(array);
    }
}
