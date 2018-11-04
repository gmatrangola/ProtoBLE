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

package com.electrazoom.protoble.mobile;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.electrazoom.protoble.central.CentralServiceException;
import com.electrazoom.protoble.central.ProtoBleCentralService;
import com.electrazoom.protoble.example.EchoUtil;

import java.util.UUID;

/**
 * Service for the EchoService This service does not do any Protobuf, it just echos the byte
 * stream to test data sizes.
 */
public class EchoBleCentralService extends ProtoBleCentralService implements EchoUtil{
    EchoListener listener;

    private static final UUID BULK_INPUT = UUID.fromString(BULK_INPUT_GUID);
    private static final UUID BULK_OUTPUT = UUID.fromString(EchoUtil.BULK_OUTPUT_GUID);

    public class LocalBinder extends Binder {
        public EchoBleCentralService getService() {
            return EchoBleCentralService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceResponseListener listener = new ServiceResponseListener();
        addInputCharacteristic(BULK_INPUT, listener);
        addOutputCharacteristic(BULK_OUTPUT, listener);
    }

    public interface EchoListener {
        void onEcho(int len, String message);
    }

    class ServiceResponseListener implements ProtoBleCentralService.ResponseListener {
        @Override
        public void onResponse(byte[] message) {
            listener.onEcho(message.length, new String(message));
        }

        @Override
        public void onError(String error) {
            // todo
        }
    }

    public void setListener(EchoListener listener) {
        this.listener = listener;
    }

    private final IBinder localBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public void writeMessage(String message) throws CentralServiceException {
        writeMessage(BULK_INPUT, message.getBytes());
    }

    public void writeMessage(byte[] bytes) throws CentralServiceException {
        writeMessage(BULK_INPUT, bytes);
    }
}
