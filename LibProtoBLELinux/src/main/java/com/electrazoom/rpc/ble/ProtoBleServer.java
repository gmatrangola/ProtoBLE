package com.electrazoom.rpc.ble;

import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import it.tangodev.ble.BleApplication;
import it.tangodev.ble.BleApplicationListener;
import it.tangodev.ble.BleService;

public class ProtoBleServer {
    private Logger LOG = LoggerFactory.getLogger(ProtoBleServer.class);

    private final Map<String, String> connectedDevices = new HashMap<>();

    private final BleApplication app;
    private final BleService service;

    private Map<String, MessageBleCharacteristic> msgCharacteristics = new HashMap<>();

    public ProtoBleServer(String appPath, String serviceGuid) {
        BleApplicationListener appListener = new BleApplicationListener() {
            @Override
            public void deviceDisconnected(String path) {
                LOG.info(appPath + " Device disconnected: " + path);
                connectedDevices.remove(path);
                for (MessageBleCharacteristic messageBleCharacteristic : msgCharacteristics.values()) {
                    messageBleCharacteristic.clear();
                    messageBleCharacteristic.onDeviceDisconnected(path);
                }
            }

            @Override
            public void deviceConnected(String path, String address) {
                LOG.info(appPath + " Device connected: " + path + " ADDR: " + address);
                connectedDevices.put(appPath, address);
                for (MessageBleCharacteristic messageBleCharacteristic : msgCharacteristics.values()) {
                    messageBleCharacteristic.onDeviceConnected(path, address);
                }
            }
        };
        app = new BleApplication(appPath, appListener);
        service = new BleService(appPath + "/service", serviceGuid, true);
    }

    public void addMessageCharacteristic(MessageBleCharacteristic characteristic) {
        characteristic.setConnectedDevices(connectedDevices);
        msgCharacteristics.put(characteristic.getName(), characteristic);
        characteristic.setService(service);
        service.addCharacteristic(characteristic);
    }

    public void startService() throws InterruptedException, DBusException {
        app.addService(service);
        app.start();
        LOG.info("App Started. Listening on adapter " + app.getBleAdapter().getAddress() + " path: "
                + app.getBleAdapter().getPath());
    }

    public Map<String, String> getConnectedDevices() {
        return connectedDevices;
    }
}
