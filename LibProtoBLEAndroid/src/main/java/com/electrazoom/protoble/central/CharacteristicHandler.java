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

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Base class for input and output BLE Characteristics
 */
abstract class CharacteristicHandler {
    protected final ProtoBleCentralService service;
    protected final ProtoBleCentralService.ResponseListener listener;
    protected UUID uuid;
    private BluetoothGattCharacteristic characteristic;

    CharacteristicHandler(ProtoBleCentralService protoBleCentralService, UUID uuid,
                          ProtoBleCentralService.ResponseListener listener) {
        this.uuid = uuid;
        this.service = protoBleCentralService;
        this.listener = listener;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public abstract void onConnect();

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public UUID getUuid() {
        return uuid;
    }
}
