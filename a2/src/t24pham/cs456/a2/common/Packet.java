package t24pham.cs456.a2.common;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import t24pham.cs456.a2.common.exceptions.PayloadTooLargeException;

public class Packet {
  public enum Type {
    DATA_PACKET(0, "DAT"),
    ACK(1, "ACK"),
    EOT(2, "EOT");

    private final byte[] code;
    private final String name;

    Type(int code, String name) {
      this.code = Utils.intToBytes(code);
      this.name = name;
    }

    public byte[] getCode() {
      return code;
    }

    public String getName() {
      return name;
    }
  }

  private Type type; // 4 bytes, big E
  private int length; // 4 bytes, big E
  private int seqNum; // 4 bytes, big E. mod 256 for data
  private byte[] payload;

  public Packet(Type type, int seqNum, byte[] payload) throws PayloadTooLargeException {
    if (payload.length > Constants.MAX_PAYLOAD_LEN) {
      throw new PayloadTooLargeException();
    }

    this.type = type;
    this.seqNum = seqNum;
    this.payload = payload;
    this.length = Constants.HEADER_LEN + this.payload.length;
  }

  public Packet(byte[] packetBytes) throws InvalidObjectException, PayloadTooLargeException {
    /* Get header bytes */
    byte[] typeBytes = Arrays.copyOfRange(packetBytes, 0, Constants.INT_LEN);
    byte[] lengthBytes = Arrays.copyOfRange(packetBytes, Constants.INT_LEN, Constants.INT_LEN * 2);
    byte[] seqNumBytes = Arrays.copyOfRange(packetBytes, Constants.INT_LEN * 2, Constants.HEADER_LEN);

    /* Convert bytes to int */
    int type = Utils.bytesToInt(typeBytes);
    int length = Utils.bytesToInt(lengthBytes);
    int seqNum = Utils.bytesToInt(seqNumBytes);

    /* Get payload based on header */
    byte[] payload = Arrays.copyOfRange(packetBytes, Constants.HEADER_LEN, length);
    if (payload.length > Constants.MAX_PAYLOAD_LEN) {
      throw new PayloadTooLargeException();
    }

    /* Set class properties */
    switch (type) {
      case 0:
        this.type = Type.DATA_PACKET;
        break;
      case 1:
        this.type = Type.ACK;
        break;
      case 2:
        this.type = Type.EOT;
        break;
      default:
        throw new InvalidObjectException("Provided packet has an invalid type");
    }
    this.length = length;
    this.seqNum = seqNum;
    this.payload = payload;
  }

  public byte[] toBytes() {
    byte[] typeBytes = type.getCode();
    byte[] lengthBytes = Utils.intToBytes(length);
    byte[] seqNumBytes = Utils.intToBytes(seqNum);

    return ByteBuffer.allocate(length)
        .put(typeBytes)
        .put(lengthBytes)
        .put(seqNumBytes)
        .put(payload)
        .array();
  }

  public Type getType() {
    return type;
  }

  public int getLength() {
    return length;
  }

  public int getSeqNum() {
    return seqNum;
  }

  public byte[] getPayload() {
    return payload;
  }

  @Override
  public String toString() {
    return String.format("%s %d %d", type.getName(), length, seqNum);
  }
}
