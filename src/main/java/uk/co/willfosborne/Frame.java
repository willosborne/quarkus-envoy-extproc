package uk.co.willfosborne;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class Frame {
    public byte opcode;
    public boolean fin;
    public boolean masked;
    public byte[] payload;

    // for generating bitmask
    private static final Random random = new Random();

    public Frame() {
        opcode = 0x01; fin = true; masked = false; payload = null;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "opcode=" + opcode +
                ", fin=" + fin +
                ", masked=" + masked +
                ", payload=" + Arrays.toString(payload) +
                ", payloadParsed=" + getPayloadAsString() +
                '}';
    }

    public String getPayloadAsString() {
        return new String(payload, Charset.defaultCharset());
    }

    public static class ConversionException extends RuntimeException {
        public ConversionException(String message) {
            super(message);
        }
    }

    private int getByteSize(final Frame frame) {
        // calculate the end size of a frame
        int length = payload.length;
        int size = 2 + (frame.masked ? 4 : 0) + length;
        if (length <= 125) {}
        else if (length >= 126 && length <= 65536) size += 2;
        else size += 8;
        return size;
    }

//    public byte[] pack(Frame frame) {
//        int offset    = -1;
//        int length    = frame.payload.length;
//        byte[] buffer = new byte[getByteSize(frame)];
//
//        // set first header: FIN & Opcode
//        buffer[0] = (byte)((frame.fin ? 0x80 : 0) | frame.opcode);
//
//        // set the payload size
//        if (length <= 125) {
//            buffer[1] = (byte)((frame.masked ? 0x80 : 0) | length);
//            offset    = 2;
//        } else if (length >= 126 && length <= 65536) {
//            buffer[1] = (byte)((frame.masked ? 0x80 : 0) | 0x7E);
//            int pos   = 2;
//            offset    = 4;
//            for (int i = 8; i > -1; i -= 8) {
//                buffer[pos] = (byte)(((i == 0) ? length : length >> i) & 0xFF);
//                pos++;
//            }
//        } else {
//            buffer[1] = (byte)((frame.masked ? 0x80 : 0) | 0x7F);
//            int pos   = 2;
//            offset    = 10;
//            for (int i = 56; i > -1; i -= 8) {
//                buffer[pos] = (byte)(((i == 0) ? length : length >> i) & 0xFF);
//                pos++;
//            }
//        }
//
//        // create mask if needed
//        byte[] mask = null;
//        if (frame.masked) {
//            mask = new byte[4];
//            random.nextBytes(mask);
//            for (int i = 0; i < mask.length; i++) {
//                buffer[offset] = mask[i];
//                offset++;
//            }
//        }
//
//        // add the payload data & mask it if necessary
//        for (int i = 0; i < length; i++)
//            buffer[offset + i] = (!frame.masked) ? frame.payload[i]
//                    : (byte)(frame.payload[i] & mask[i % 4]);
//
//        // return the packed data
//        return buffer;
//    }

    public static Frame parse(byte[] raw) {
        // Create new frame & wrap byte data in buffer for ease of use
        Frame frame     = new Frame();
        ByteBuffer data = ByteBuffer.wrap(raw);

        // Get FIN + RSV + Opcode as bytes
        byte buffer  = data.get();
        frame.fin    = (buffer & 0x80) != 0;
        frame.opcode = (byte)(buffer & 0x0f);
        if ((buffer & 0x40) != 0 || // rsv1
                (buffer & 0x20) != 0 || // rsv2
                (buffer & 0x10) != 0  ) // rsv3
            throw new ConversionException("Reserved bytes should not be set!");

        // Get Mask and payload length
        buffer        = data.get();
        frame.masked  = (buffer & 0x80) != 0;
        int dataSize  = (byte)(0x7F & buffer);
        int dataCount = 0;

        // Get extended length range
        if (dataSize == 0x7F)
            dataCount = 8;
        else if (dataSize == 0x7E)
            dataCount = 2;
        while (--dataCount > 0)
            dataSize |= (data.get() & 0xFF) << (8 * dataCount);

        // Get masking key
        byte[] maskingKey = null;
        if (frame.masked) {
            maskingKey = new byte[4];
            data.get(maskingKey, 0, maskingKey.length);
        }

        // fetch data and demask if needed
        frame.payload = new byte[dataSize];
        data.get(frame.payload, 0, dataSize);
        if (frame.masked)
            for (int i = 0; i < frame.payload.length; i++)
                frame.payload[i] ^= maskingKey[i % 4];

        // return parsed frame
        return frame;
    }
}
