package com.electrazoom.rpc.ble;

import com.electrazoom.protoble.valuemessage.OutputMessageQueue;

import org.apache.commons.codec.binary.Hex;
import org.bluez.GattCharacteristic1;
import org.freedesktop.DBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import it.tangodev.ble.BleService;

/**
 * Provides a characteristic that publishes the message type
 */
public class MessageOutputBleCharacteristic extends MessageBleCharacteristic implements
        GattCharacteristic1, DBus.Properties {
    private static final Logger LOG = LoggerFactory.getLogger(MessageOutputBleCharacteristic.class);

    private OutputMessageQueue outputQueue = new OutputMessageQueue();

    public MessageOutputBleCharacteristic(String name, String charUuId) {
        super(name);
        this.uuid = charUuId;
        List<CharacteristicFlag> flags = new ArrayList<CharacteristicFlag>();
        flags.add(CharacteristicFlag.READ);
        flags.add(CharacteristicFlag.NOTIFY);
        setFlags(flags);
    }

    public void sendMessage(byte[] bytes) {
        LOG.debug("sendMessage " + Hex.encodeHexString(bytes));
        outputQueue.add(bytes);
        if (!getConnectedDevices().isEmpty()) sendNotification(null);
    }

    @Override
    public void setService(BleService service) {
        super.setService(service);
        this.path = service.getPath() + "/" + name;
    }

    @Override
    protected byte[] onReadValue(String devicePath) {
        LOG.debug("onReadValue start buffered = " + outputQueue.buffersRemaining());
        byte[] array = null;
        try {
            array = outputQueue.getNextValue();
        }
        catch (Exception e) {
            LOG.error("Write value from queue", e);
        }
        if (array == null) array = new byte[0];
        LOG.debug("onReadValue end buffered = " + outputQueue.buffersRemaining() + " array=" +
                Hex.encodeHexString(array));
        return array;
    }

    public String getName() {
        return name;
    }

    @Override
    public void clear() {
        outputQueue.clear();
    }

    @Override
    public void onDeviceConnected(String path, String address) {

    }

    @Override
    public void onDeviceDisconnected(String path) {

    }
}
