package com.electrazoom.protoble.example;

import com.electrazoom.rpc.ble.MessageInputBleCharacteristic;
import com.electrazoom.rpc.ble.MessageOutputBleCharacteristic;
import com.electrazoom.rpc.ble.ProtoBleServer;
import static com.electrazoom.protoble.example.EchoUtil.*;

import org.apache.commons.codec.binary.Hex;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple test byte array test service
 */
public class EchoBytesTestServer {
    private static final Logger LOG = LoggerFactory.getLogger(EchoBytesTestServer.class);

    private static final String APP_PATH = "/bulktest";

    private final ProtoBleServer protoServer;
    private final MessageInputBleCharacteristic inputCharacteristic;
    private final MessageOutputBleCharacteristic outputCharacteristic;

    private EchoBytesTestServer() {
        protoServer = new ProtoBleServer(APP_PATH, BULK_SERVICE_GUID);
        inputCharacteristic = new MessageInputBleCharacteristic(protoServer.getService(), "bulkIn", BULK_INPUT_GUID);
        outputCharacteristic = new MessageOutputBleCharacteristic(protoServer.getService(), "bulkOut", BULK_OUTPUT_GUID);

        inputCharacteristic.setInputListener(new MessageInputBleCharacteristic.InputListener() {
            @Override
            public void onServiceConnected() {
                LOG.info("ServicConnected");
            }

            @Override
            public void onError(String source, String error) {
                LOG.error("Error: " + error);
            }

            @Override
            public void onMessageInput(byte[] message) {
                LOG.debug("onMessageInput " + Hex.encodeHexString(message));
                outputCharacteristic.sendMessage(message);
            }
        });
        protoServer.addMessageCharacteristic(inputCharacteristic);
        protoServer.addMessageCharacteristic(outputCharacteristic);
    }

    private void start() throws DBusException, InterruptedException {
        protoServer.startService();
        byte[] bytes = new byte[25];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0x1f;
        }
        outputCharacteristic.sendMessage(bytes);
    }

    public static void main(String... args) {
        EchoBytesTestServer server = new EchoBytesTestServer();
        try {
            server.start();
        } catch (Exception e) {
            LOG.error("Error starting service", e);
        }

    }
}
