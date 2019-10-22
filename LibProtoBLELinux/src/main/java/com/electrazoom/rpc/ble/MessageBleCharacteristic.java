package com.electrazoom.rpc.ble;

import java.util.Map;

import it.tangodev.ble.BleCharacteristic;
import it.tangodev.ble.BleService;

/**
 * Base Class for Protobuf Message BLE Characteristics
 */
abstract class MessageBleCharacteristic extends BleCharacteristic {
    protected final String name;
    private Map<String, String> connectedDevices;

    public MessageBleCharacteristic(BleService service, String name) {
        super(service);
        this.name = name;
        this.path = service.getPath() + "/" + name;
    }

    public abstract void clear();
    public abstract void onDeviceConnected(String path, String address);
    public abstract void onDeviceDisconnected(String path);

    public String getName() {
        return name;
    }

    public void setConnectedDevices(Map<String, String> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    public Map<String, String> getConnectedDevices() {
        return connectedDevices;
    }
}
