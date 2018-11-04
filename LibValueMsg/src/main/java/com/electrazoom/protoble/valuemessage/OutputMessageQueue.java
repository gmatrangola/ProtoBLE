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

package com.electrazoom.protoble.valuemessage;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Queues up messages to send over BLE through a Characteristic value that only
 * has 20 bytes
 */
public class OutputMessageQueue {
    private static final Logger LOG = LoggerFactory.getLogger(OutputMessageQueue.class);

    public static final short MAX_VALUE_LEN = 19;
    public static final short HEADER_SIZZE = 2;
    public static final short FIRST_BUFFSIZE = MAX_VALUE_LEN-HEADER_SIZZE;

    private BlockingQueue<ByteBuffer> buffers = new LinkedBlockingDeque<>();

    public boolean add(byte[] bytes) {
        LOG.debug("add: len = " + bytes.length + " value = " + new String(Hex.encodeHex(bytes)));
        boolean isNew = buffers.isEmpty();
        short totalMessageLen = (short) (bytes.length);
        short buffsize;
        if ((totalMessageLen + HEADER_SIZZE) < MAX_VALUE_LEN) {
            buffsize = (short) (totalMessageLen +HEADER_SIZZE);
        }
        else {
            buffsize = MAX_VALUE_LEN;
        }
        ByteBuffer firstBuffer = ByteBuffer.allocate(buffsize);
        short bytesWritten = (short) (buffsize - HEADER_SIZZE);
        LOG.debug("   bytesWritten = " + bytesWritten + " totalMessageLen = " + totalMessageLen);
        firstBuffer.putShort(totalMessageLen);
        firstBuffer.put(bytes, 0, bytesWritten);
        buffers.add(firstBuffer);
        int offset = bytesWritten;
        int remainder = bytes.length - bytesWritten;
        while (remainder > 0) {
            LOG.debug("    remainder = " + remainder);
            if (remainder > MAX_VALUE_LEN) bytesWritten = MAX_VALUE_LEN;
            else bytesWritten = (short) remainder;
            ByteBuffer buffer = ByteBuffer.allocate(bytesWritten);
            buffer.put(bytes, offset, bytesWritten);
            buffers.add(buffer);
            offset += bytesWritten;
            remainder = bytes.length - offset;
        }
        return isNew;
    }

    /**
     * Get the next buffer to set as Characteristic value.
     * If there are no more buffers in the current message, start buffering
     * the next message.
     * If there are no more buffers in the current message, and no more message to
     * buffer, return null
     * @return byte[] next message buffer, or null if no more buffers or messages
     */
    public byte[] getNextValue() {
        byte[] array = null;
        if (buffers.isEmpty()) {
            return null;
        }

        ByteBuffer buffer = buffers.remove();
        array = buffer.array();
        LOG.debug("getNextValue: length: " + array.length + " bytes:" + new String(Hex.encodeHex(array)));
        return array;
    }

    public byte[] waitForNextValue() throws InterruptedException {
        byte[] array = buffers.take().array();
        LOG.debug("waitForNextValue: " + new String(Hex.encodeHex(array)));
        return array;
    }


    public int buffersRemaining() {
        return buffers.size();
    }

    public void clear() {
        buffers.clear();
    }
}
