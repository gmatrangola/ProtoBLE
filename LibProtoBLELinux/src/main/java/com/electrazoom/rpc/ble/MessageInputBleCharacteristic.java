package com.electrazoom.rpc.ble;

import it.tangodev.ble.BleService;

import org.apache.commons.codec.binary.Hex;
import org.bluez.GattCharacteristic1;
import org.freedesktop.DBus;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.electrazoom.protoble.valuemessage.OutputMessageQueue.FIRST_BUFFSIZE;

/**
 * BLE Characteristic that will accept input from the Central/Client
 */
public class MessageInputBleCharacteristic extends MessageBleCharacteristic implements GattCharacteristic1, DBus.Properties {
    private static final Logger LOG = LoggerFactory.getLogger(MessageInputBleCharacteristic.class);

    public interface InputListener {
        void onServiceConnected();
        void onError(String source, String error);
        void onMessageInput(byte[] message);
    }

    private short currentMessageLen = 0;
    private int currentBytesRead = 0;
    private ByteBuffer currentBuffer;
    private InputListener inputListener;

    public MessageInputBleCharacteristic(BleService service, String name, String charUuid) {
        super(service, name);
        this.uuid = charUuid;
        List<CharacteristicFlag> flags = new ArrayList<CharacteristicFlag>();
        flags.add(CharacteristicFlag.WRITE);
        setFlags(flags);
    }

    @Override
    public void setService(BleService service) {
        super.setService(service);
        this.path = service.getPath() + "/" + name;
    }

    @Override
    protected void onWriteValue(String devicePath, int offset, byte[] value) {
        try {
            handleInputMessage(value);
        }
        catch (Exception e) {
            LOG.error("Error handling Write " + value.length, e);
        }
    }

    private void handleInputMessage(byte[] value) {
        LOG.debug("handleInputMessage " + value.length + ": " + Hex.encodeHexString(value));
        LOG.debug("handleInputMessage " + value.length + " currentMessageLen = " + currentMessageLen
                + " currentBuffer " + currentBuffer + " currentBytesRead " + currentBytesRead);
        if (currentMessageLen == 0) readFirstBuffer(value);
        else {
            currentBuffer.put(value);
            currentBytesRead += value.length;
            LOG.debug("  handleInputMessage currenBytesRead = " + currentBytesRead);
            if (currentBytesRead >= currentMessageLen) {
                notifyMessage(currentBuffer.array());
                clear();
            }
        }
    }

    private void readFirstBuffer(byte[] value) {
        ByteBuffer firstBuffer = ByteBuffer.wrap(value);
        currentMessageLen = firstBuffer.getShort();
        LOG.debug("readFirstMessage " + value.length + " len=" + currentMessageLen);
        if (currentMessageLen <= FIRST_BUFFSIZE) {
            LOG.debug("  readFirstMessage single part");
            byte[] array = new byte[currentMessageLen];
            firstBuffer.get(array);
            currentMessageLen = 0;
            notifyMessage(array);
        }
        else {
            LOG.debug("  readFirstMessage multipart");
            byte[] array = new byte[FIRST_BUFFSIZE];
            firstBuffer.get(array);
            currentBuffer = ByteBuffer.allocate(currentMessageLen);
            currentBytesRead = array.length;
            currentBuffer.put(array);
        }
    }

    @Override
    public void clear() {
        LOG.debug("clear()");
        currentBuffer = null;
        currentBytesRead = 0;
        currentMessageLen = 0;
    }

    @Override
    public void onDeviceConnected(String path, String address) {

    }

    @Override
    public void onDeviceDisconnected(String path) {

    }

    private void notifyMessage(byte[] array) {
        if(inputListener != null) inputListener.onMessageInput(array);
    }

    @Override
    public byte[] ReadValue(Map<String, Variant> option) {
        throw new InvalidOperation("No read is permitted from Input");
    }

    public InputListener getInputListener() {
        return inputListener;
    }

    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }
}
