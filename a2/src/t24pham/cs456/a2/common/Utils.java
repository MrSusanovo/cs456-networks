package t24pham.cs456.a2.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
  public static byte[] intToBytes(int i) {
    return ByteBuffer.allocate(Constants.INT_LEN).order(ByteOrder.BIG_ENDIAN)
        .putInt(i).array();
  }

  public static int bytesToInt(byte[] bytes) {
    return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        .getInt();
  }

  public static void error(String msg) {
    System.err.println("Error: " + msg);
  }
}
